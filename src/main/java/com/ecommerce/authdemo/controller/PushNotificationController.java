package com.ecommerce.authdemo.controller;

import com.ecommerce.authdemo.dto.ApiResponse;
import com.ecommerce.authdemo.dto.PushNotificationRequest;
import com.ecommerce.authdemo.dto.PushNotificationResponse;
import com.ecommerce.authdemo.service.PushNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/push-notifications")
@RequiredArgsConstructor
public class PushNotificationController {

    private final PushNotificationService pushNotificationService;

    @PostMapping
    public ResponseEntity<ApiResponse<PushNotificationResponse>> create(
            @Valid @RequestBody PushNotificationRequest request) {
        PushNotificationResponse data = pushNotificationService.create(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Push notification created successfully", data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PushNotificationResponse>>> getNotifications(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean isRead) {
        List<PushNotificationResponse> data = pushNotificationService.getNotifications(userId, type, isRead);
        return ResponseEntity.ok(new ApiResponse<>(true, "Push notifications fetched successfully", data));
    }

    @GetMapping("/paged")
    public ResponseEntity<ApiResponse<Page<PushNotificationResponse>>> getNotificationsPaged(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean isRead,
            Pageable pageable) {
        Page<PushNotificationResponse> data = pushNotificationService.getNotificationsPaged(userId, type, isRead, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Push notifications fetched successfully", data));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<PushNotificationResponse>> markAsRead(@PathVariable Integer id) {
        PushNotificationResponse data = pushNotificationService.markAsRead(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Push notification marked as read", data));
    }

    @PatchMapping("/{id}/unread")
    public ResponseEntity<ApiResponse<PushNotificationResponse>> markAsUnread(@PathVariable Integer id) {
        PushNotificationResponse data = pushNotificationService.markAsUnread(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Push notification marked as unread", data));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Integer>> markAllAsRead(@RequestParam Long userId) {
        int data = pushNotificationService.markAllAsRead(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "All notifications marked as read", data));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@RequestParam Long userId) {
        long data = pushNotificationService.getUnreadCount(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Unread notification count fetched successfully", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Integer id) {
        pushNotificationService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Push notification deleted successfully", "OK"));
    }
}
