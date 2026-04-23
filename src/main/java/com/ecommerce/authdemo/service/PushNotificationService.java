package com.ecommerce.authdemo.service;

import com.ecommerce.authdemo.dto.PushNotificationRequest;
import com.ecommerce.authdemo.dto.PushNotificationResponse;

import java.util.List;

public interface PushNotificationService {
    PushNotificationResponse create(PushNotificationRequest request);

    List<PushNotificationResponse> getNotifications(Integer userId, String type, Boolean isRead);

    PushNotificationResponse markAsRead(Integer id);

    PushNotificationResponse markAsUnread(Integer id);

    void delete(Integer id);
}
