package com.ecommerce.authdemo.controller;

import com.ecommerce.authdemo.service.RazorpayService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@CrossOrigin("*")
public class PaymentController {

    private final RazorpayService razorpayService;

    // ✅ 1. CREATE ORDER (IMPORTANT FOR FRONTEND)
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestParam Double amount) {

        try {
            JSONObject order = razorpayService.createOrder(amount);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Order created successfully");
            response.put("data", order.toMap());

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to create order");

            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ✅ 2. VERIFY PAYMENT
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            @RequestParam String orderId,
            @RequestParam String paymentId,
            @RequestParam String signature) {

        try {
            boolean success = razorpayService.verifyPayment(orderId, paymentId, signature);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "Payment successful" : "Payment failed");

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error verifying payment");

            return ResponseEntity.internalServerError().body(response);
        }
    }
}