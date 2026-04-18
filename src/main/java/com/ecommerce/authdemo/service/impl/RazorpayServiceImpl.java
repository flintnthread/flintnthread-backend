package com.ecommerce.authdemo.service.impl;

import com.ecommerce.authdemo.entity.PaymentTransaction;
import com.ecommerce.authdemo.entity.RazorpayConfig;
import com.ecommerce.authdemo.repository.PaymentTransactionRepository;
import com.ecommerce.authdemo.repository.RazorpayConfigRepository;
import com.ecommerce.authdemo.service.RazorpayService;
import com.razorpay.*;

import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RazorpayServiceImpl implements RazorpayService {

    private final RazorpayConfigRepository configRepo;
    private final PaymentTransactionRepository paymentTransactionRepo;

    // ✅ CREATE ORDER
    @Override
    public JSONObject createOrder(Double amount) {

        try {
            RazorpayConfig config = configRepo.findTopByOrderByIdDesc()
                    .orElseThrow(() -> new RuntimeException("Razorpay config not found"));

            RazorpayClient client = new RazorpayClient(
                    config.getKeyId(),
                    config.getKeySecret()
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
            RazorpayConfig config = configRepo.findTopByOrderByIdDesc()
                    .orElseThrow(() -> new RuntimeException("Razorpay config not found"));

            String data = orderId + "|" + paymentId;

            boolean isValid = Utils.verifySignature(data, signature, config.getKeySecret());

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