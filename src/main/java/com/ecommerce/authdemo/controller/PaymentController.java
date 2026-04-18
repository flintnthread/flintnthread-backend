
package com.ecommerce.authdemo.controller;

import com.ecommerce.authdemo.service.RazorpayService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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

    @Value("${razorpay.key_id:}")
    private String razorpayKeyId;

    private static Double resolveAmount(Double queryAmount, Map<String, Object> body) {
        if (queryAmount != null) {
            return queryAmount;
        }
        if (body == null || !body.containsKey("amount")) {
            return null;
        }
        Object raw = body.get("amount");
        if (raw == null) {
            return null;
        }
        if (raw instanceof Number n) {
            return n.doubleValue();
        }
        if (raw instanceof String s) {
            try {
                return Double.parseDouble(s.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    /**
     * Browsers and some clients call URLs with GET. Payment creation must be POST only.
     */
    @GetMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createOrderGetNotAllowed() {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", "Use HTTP POST, not GET. Opening this URL in a browser always sends GET.");
        body.put("method", "POST");
        body.put("urlExample", "/api/payment/create-order?amount=1");
        body.put("bodyExample", "{\"amount\":1}");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
    }

    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyPaymentGetNotAllowed() {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", "Use HTTP POST, not GET.");
        body.put("method", "POST");
        body.put("urlExample", "/api/payment/verify?orderId=...&paymentId=...&signature=...");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
    }

    // ✅ 1. CREATE ORDER — amount: query ?amount=100 and/or JSON body {"amount":100}
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(
            @RequestParam(required = false) Double amount,
            @RequestBody(required = false) Map<String, Object> body) {

        Double resolved = resolveAmount(amount, body);
        if (resolved == null || resolved <= 0) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Missing or invalid amount");
            err.put("hint", "Use query only in the URL bar: http://localhost:8080/api/payment/create-order?amount=1 (do not type POST there). Or Body → raw JSON: {\"amount\":1}");
            return ResponseEntity.badRequest().body(err);
        }

        try {
            JSONObject order = razorpayService.createOrder(resolved);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Order created successfully");
            response.put("data", order.toMap());
            response.put("razorpayKeyId", razorpayKeyId);

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