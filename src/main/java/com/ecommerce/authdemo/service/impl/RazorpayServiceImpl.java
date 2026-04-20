package com.ecommerce.authdemo.service.impl;

import com.ecommerce.authdemo.entity.PaymentTransaction;
import com.ecommerce.authdemo.entity.RazorpayConfig;
import com.ecommerce.authdemo.repository.PaymentTransactionRepository;
import com.ecommerce.authdemo.repository.RazorpayConfigRepository;
import com.ecommerce.authdemo.service.RazorpayService;
import com.razorpay.*;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Service
public class RazorpayServiceImpl implements RazorpayService {

    private final RazorpayConfigRepository configRepo;
    private final PaymentTransactionRepository paymentTransactionRepo;
    private final String keyIdProp;
    private final String keySecretProp;

    public RazorpayServiceImpl(
            RazorpayConfigRepository configRepo,
            PaymentTransactionRepository paymentTransactionRepo,
            @Value("${razorpay.key_id:}") String keyIdProp,
            @Value("${razorpay.key_secret:}") String keySecretProp) {
        this.configRepo = configRepo;
        this.paymentTransactionRepo = paymentTransactionRepo;
        this.keyIdProp = keyIdProp;
        this.keySecretProp = keySecretProp;
    }

    private String resolveKeyId() {
        if (StringUtils.hasText(keyIdProp)) {
            return keyIdProp.trim();
        }
        return configRepo.findTopByOrderByIdDesc()
                .map(RazorpayConfig::getKeyId)
                .filter(StringUtils::hasText)
                .orElseThrow(() -> new RuntimeException("Set razorpay.key_id in application.properties or add razorpay_config row"));
    }

    private String resolveKeySecret() {
        if (StringUtils.hasText(keySecretProp)) {
            return keySecretProp.trim();
        }
        return configRepo.findTopByOrderByIdDesc()
                .map(RazorpayConfig::getKeySecret)
                .filter(StringUtils::hasText)
                .orElseThrow(() -> new RuntimeException("Set razorpay.key_secret in application.properties or add razorpay_config row"));
    }

    // ✅ CREATE ORDER
    @Override
    public JSONObject createOrder(Double amount) {

        try {
            RazorpayClient client = new RazorpayClient(
                    resolveKeyId(),
                    resolveKeySecret()
            );

            JSONObject options = new JSONObject();
            options.put("amount", (int) (amount * 100));
            options.put("currency", "INR");
            options.put("receipt", "txn_" + System.currentTimeMillis());

            Order order = client.orders.create(options);

            // ✅ SAVE PAYMENT TRANSACTION
            PaymentTransaction pt = PaymentTransaction.builder()
                    .orderId(0) // replace later
                    .transactionId(order.get("id"))
                    .paymentMethod("RAZORPAY")
                    .amount(BigDecimal.valueOf(amount))
                    .currency("INR")
                    .status("CREATED")
                    .responseData(order.toString())
                    .build();

            paymentTransactionRepo.save(pt);

            return order.toJson();

        } catch (Exception e) {
            throw new RuntimeException("Error creating Razorpay order", e);
        }
    }

    // ✅ VERIFY PAYMENT
    @Override
    public boolean verifyPayment(String orderId, String paymentId, String signature) {

        try {
            String data = orderId + "|" + paymentId;

            boolean isValid = Utils.verifySignature(data, signature, resolveKeySecret());

            if (isValid) {

                PaymentTransaction pt = paymentTransactionRepo
                        .findByTransactionId(orderId);

                if (pt != null) {
                    pt.setStatus("SUCCESS");
                    pt.setResponseData("paymentId=" + paymentId);
                    paymentTransactionRepo.save(pt);
                }
            }

            return isValid;

        } catch (Exception e) {
            return false;
        }
    }
}