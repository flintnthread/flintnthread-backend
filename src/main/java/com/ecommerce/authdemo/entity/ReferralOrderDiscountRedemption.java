package com.ecommerce.authdemo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "referral_order_discount_redemptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferralOrderDiscountRedemption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "referrer_user_id", nullable = false)
    private Long referrerUserId;

    @Column(name = "required_referrals")
    private Integer requiredReferrals;

    @Column(name = "referral_count_at_unlock")
    private Integer referralCountAtUnlock;

    @Column(name = "discount_percent")
    private Double discountPercent;

    @Column(name = "is_used")
    private Boolean used;

    @Column(name = "used_order_id")
    private Long usedOrderId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;
}

