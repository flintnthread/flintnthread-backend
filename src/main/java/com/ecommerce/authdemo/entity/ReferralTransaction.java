package com.ecommerce.authdemo.entity;



import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

    @Entity
    @Table(name = "referral_transactions")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class ReferralTransaction {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        @Column(name = "referrer_id", nullable = false)
        private Integer referrerId;

        @Column(name = "referred_user_id", nullable = false)
        private Integer referredUserId;

        @Column(name = "amount", nullable = false, precision = 10, scale = 2)
        private BigDecimal amount;

        @Enumerated(EnumType.STRING)
        @Column(name = "transaction_type")
        private TransactionType transactionType;

        @Enumerated(EnumType.STRING)
        @Column(name = "status")
        private Status status;

        @Column(name = "description", columnDefinition = "TEXT")
        private String description;

        @Column(name = "created_at", insertable = false, updatable = false)
        private LocalDateTime createdAt;

        // 🔥 ENUMS
        public enum TransactionType {
            referral_bonus,
            signup_bonus,
            order_usage,
            admin_adjustment
        }

        public enum Status {
            pending,
            completed,
            cancelled
        }
    }

