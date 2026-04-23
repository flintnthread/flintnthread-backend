package com.ecommerce.authdemo.controller;

import com.ecommerce.authdemo.dto.ApiResponse;
import com.ecommerce.authdemo.dto.ShiprocketSyncLogDTO;
import com.ecommerce.authdemo.service.ShiprocketSyncLogService;
import com.ecommerce.authdemo.service.ShiprocketWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shiprocket")
@RequiredArgsConstructor
@CrossOrigin("*")
@Slf4j
public class ShiprocketWebhookController {

    private final ShiprocketWebhookService shiprocketWebhookService;
    private final ShiprocketSyncLogService shiprocketSyncLogService;

    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<Map<String, Object>>> receiveWebhook(
            @RequestBody(required = false) Map<String, Object> payload) {
        try {
            shiprocketWebhookService.handleWebhook(payload);
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Webhook received",
                    Map.of("processed", true)
            ));
        } catch (Exception e) {
            log.error("Failed to process Shiprocket webhook", e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false,
                    "Failed to process webhook",
                    Map.of("processed", false)
            ));
        }
    }

    @GetMapping("/sync-logs")
    public ResponseEntity<ApiResponse<List<ShiprocketSyncLogDTO>>> getSyncLogs(
            @RequestParam(required = false) Integer orderId,
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate) {
        try {
            List<ShiprocketSyncLogDTO> logs = shiprocketSyncLogService.getSyncLogs(
                    orderId, orderNumber, fromDate, toDate
            );
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Sync logs fetched successfully",
                    logs
            ));
        } catch (Exception e) {
            log.error("Failed to fetch Shiprocket sync logs", e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false,
                    "Failed to fetch sync logs",
                    List.of()
            ));
        }
    }
}
