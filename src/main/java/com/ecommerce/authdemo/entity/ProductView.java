package com.ecommerce.authdemo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

    @Data
    @Entity
    @Table(name = "product_views")
    public class ProductView {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name="product_id")
        private Long productId;

        @Column(name="user_id")
        private Long userId;

        @Column(name="session_id")
        private String sessionId;

        @Column(name="ip_address")
        private String ipAddress;

        @Column(name="user_agent")
        private String userAgent;

        @Column(name="viewed_at")
        private LocalDateTime viewedAt;

        @PrePersist
        public void prePersist() {
            this.viewedAt = LocalDateTime.now();
        }
    }

