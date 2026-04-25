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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

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
            options.put("amount", (int) Math.round(amount * 100.0));
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

    @Override
    public boolean verifyPayment(String orderId, String paymentId, String signature) {
        if (!StringUtils.hasText(orderId) || !StringUtils.hasText(paymentId) || !StringUtils.hasText(signature)) {
            return false;
        }
        try {
            String secret = resolveKeySecret();
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b & 0xff));
            }
            return constantTimeEquals(hex.toString(), signature.trim().toLowerCase());
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Could not verify Razorpay signature", e);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }
        int r = 0;
        for (int i = 0; i < a.length(); i++) {
            r |= a.charAt(i) ^ b.charAt(i);
        }
        return r == 0;
    }
}