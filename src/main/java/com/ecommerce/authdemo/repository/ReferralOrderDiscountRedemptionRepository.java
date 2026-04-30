package com.ecommerce.authdemo.repository;

import com.ecommerce.authdemo.entity.ReferralOrderDiscountRedemption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReferralOrderDiscountRedemptionRepository extends JpaRepository<ReferralOrderDiscountRedemption, Integer> {

    Optional<ReferralOrderDiscountRedemption> findTopByReferrerUserIdAndUsedFalseOrderByCreatedAtDesc(Long referrerUserId);

    Optional<ReferralOrderDiscountRedemption> findTopByReferrerUserIdOrderByCreatedAtDesc(Long referrerUserId);

    boolean existsByReferrerUserIdAndUsedTrue(Long referrerUserId);

    boolean existsByReferrerUserIdAndUsedFalse(Long referrerUserId);
}

