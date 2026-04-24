package com.ecommerce.authdemo.controller;

import com.ecommerce.authdemo.service.OrderService;
import com.ecommerce.authdemo.service.ShiprocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/shiprocket")
@RequiredArgsConstructor
@CrossOrigin("*")
@Slf4j
public class ShiprocketWebhookController {

    private final ShiprocketService shiprocketService;
    private final OrderService orderService;

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> receiveWebhook(
            @RequestBody(required = false) Map<String, Object> payload) {
        try {
            log.info("Received Shiprocket webhook: {}", payload);
            
            if (payload == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Empty webhook payload"
                ));
            }

            // Process webhook data
            shiprocketService.handleWebhook(payload);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Webhook processed successfully",
                "processed", true
            ));
            
        } catch (Exception e) {
            log.error("Failed to process Shiprocket webhook", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to process webhook",
                "processed", false
            ));
        }
    }

    @GetMapping("/track/{awb}")
    public ResponseEntity<Map<String, Object>> trackShipment(@PathVariable String awb) {
        try {
            String trackingInfo = shiprocketService.trackShipment(awb);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Tracking information retrieved",
                "data", trackingInfo
            ));
            
        } catch (Exception e) {
            log.error("Failed to track shipment AWB: {}", awb, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to track shipment"
            ));
        }
    }
}
