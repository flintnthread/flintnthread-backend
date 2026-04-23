package com.ecommerce.authdemo.repository;

import com.ecommerce.authdemo.entity.PushNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PushNotificationRepository extends JpaRepository<PushNotification, Integer> {

    @Query("""
            SELECT p
            FROM PushNotification p
            WHERE (:userId IS NULL OR p.userId = :userId)
              AND (:type IS NULL OR LOWER(p.type) = LOWER(:type))
              AND (:isRead IS NULL OR p.isRead = :isRead)
            ORDER BY p.createdAt DESC
            """)
    List<PushNotification> findWithFilters(@Param("userId") Integer userId,
                                           @Param("type") String type,
                                           @Param("isRead") Boolean isRead);
}
