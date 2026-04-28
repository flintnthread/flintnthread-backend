package com.ecommerce.authdemo.service.impl;

import com.ecommerce.authdemo.dto.PushNotificationRequest;
import com.ecommerce.authdemo.dto.PushNotificationResponse;
import com.ecommerce.authdemo.entity.PushNotification;
import com.ecommerce.authdemo.exception.ResourceNotFoundException;
import com.ecommerce.authdemo.repository.PushNotificationRepository;
import com.ecommerce.authdemo.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PushNotificationServiceImpl implements PushNotificationService {

    private final PushNotificationRepository pushNotificationRepository;

    @Override
    public PushNotificationResponse create(PushNotificationRequest request) {
        PushNotification entity = PushNotification.builder()
                .userId(request.getUserId())
                .title(request.getTitle().trim())
                .message(request.getMessage().trim())
                .type(normalize(request.getType()))
                .link(normalize(request.getLink()))
                .build();
        return toResponse(pushNotificationRepository.save(entity));
    }

    @Override
    public List<PushNotificationResponse> getNotifications(Long userId, String type, Boolean isRead) {
        return pushNotificationRepository.findWithFilters(userId, normalize(type), isRead)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public Page<PushNotificationResponse> getNotificationsPaged(Long userId, String type, Boolean isRead, Pageable pageable) {
        return pushNotificationRepository.findPageWithFilters(userId, normalize(type), isRead, pageable)
                .map(this::toResponse);
    }

    @Override
    public PushNotificationResponse markAsRead(Integer id) {
        PushNotification entity = pushNotificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Push notification not found"));
        entity.setIsRead(Boolean.TRUE);
        entity.setReadAt(LocalDateTime.now());
        return toResponse(pushNotificationRepository.save(entity));
    }

    @Override
    public PushNotificationResponse markAsUnread(Integer id) {
        PushNotification entity = pushNotificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Push notification not found"));
        entity.setIsRead(Boolean.FALSE);
        entity.setReadAt(null);
        return toResponse(pushNotificationRepository.save(entity));
    }

    @Override
    @Transactional
    public int markAllAsRead(Long userId) {
        return pushNotificationRepository.markAllAsReadByUserId(userId, LocalDateTime.now());
    }

    @Override
    public long getUnreadCount(Long userId) {
        return pushNotificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    public void delete(Integer id) {
        PushNotification entity = pushNotificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Push notification not found"));
        pushNotificationRepository.delete(entity);
    }

    private PushNotificationResponse toResponse(PushNotification entity) {
        return PushNotificationResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .title(entity.getTitle())
                .message(entity.getMessage())
                .type(entity.getType())
                .link(entity.getLink())
                .isRead(entity.getIsRead())
                .createdAt(entity.getCreatedAt())
                .readAt(entity.getReadAt())
                .build();
    }

    private String normalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
