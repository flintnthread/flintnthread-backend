package com.ecommerce.authdemo.service;

import com.ecommerce.authdemo.dto.ReferralOverviewDTO;

public interface ReferralService {

    void giveSignupBonus(Integer referrerId, Integer newUserId);

    ReferralOverviewDTO getMyReferralOverview();

    ReferralOverviewDTO applyReferralCode(String referralCode);

    void confirmReferralOnFirstPaidOrder(Long referredUserId);

    double getAvailableReferralDiscountPercentForUser(Long userId);

    void markReferralDiscountUsed(Long userId, Long orderId);
}

