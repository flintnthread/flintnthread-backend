package com.ecommerce.authdemo.service.impl;

import com.ecommerce.authdemo.dto.ReferralDashboardDto;
import com.ecommerce.authdemo.dto.ReferralResponse;
import com.ecommerce.authdemo.dto.ShareDto;
import com.ecommerce.authdemo.entity.ReferralOrderDiscountRedemption;
import com.ecommerce.authdemo.entity.ReferralTransaction;
import com.ecommerce.authdemo.entity.User;
import com.ecommerce.authdemo.repository.ReferralDiscountRepository;
import com.ecommerce.authdemo.repository.ReferralTransactionRepository;
import com.ecommerce.authdemo.repository.UserRepository;
import com.ecommerce.authdemo.service.ReferralService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class ReferralServiceImpl implements ReferralService {

    private final UserRepository userRepository;
    private final ReferralTransactionRepository transactionRepository;
    private final ReferralDiscountRepository discountRepository;

    private static final int TARGET_REFERRALS = 5;
    private static final BigDecimal DISCOUNT_PERCENT = new BigDecimal("0.10");

    @Override
    public void generateCodes(Long userId, String username) {

        User user = userRepository.findById(userId).orElseThrow();

        user.setCompanyReferenceId(
                "f&t" + String.format("%06d", userId)
        );

        String shortName = username == null ? "USER"
                : username.replaceAll("\\s+", "")
                .toUpperCase();

        if (shortName.length() > 4) {
            shortName = shortName.substring(0, 4);
        }

        String code = "REF" +
                shortName +
                String.format("%06d", userId);

        user.setReferralCode(code);

        userRepository.save(user);
    }

    @Override
    public ReferralResponse applyReferral(Long userId, String referralCode) {

        if (transactionRepository.existsByReferredUserId(userId)) {
            return new ReferralResponse(false, "Referral already used");
        }

        User referrer = userRepository
                .findByReferralCode(referralCode)
                .orElseThrow(() ->
                        new RuntimeException("Invalid referral code"));

        if (referrer.getId().equals(userId)) {
            return new ReferralResponse(false, "Own code not allowed");
        }

        ReferralTransaction tx = new ReferralTransaction();

        tx.setReferrerId(referrer.getId());
        tx.setReferredUserId(userId);
        tx.setAmount(BigDecimal.ZERO);
        tx.setTransactionType(
                ReferralTransaction.TransactionType.signup_bonus
        );
        tx.setStatus(
                ReferralTransaction.Status.completed
        );
        tx.setDescription("Referral joined");

        transactionRepository.save(tx);

        return new ReferralResponse(
                true,
                "Referral applied successfully"
        );
    }

    @Override
    public BigDecimal calculateFirstOrderDiscount(
            Long userId,
            BigDecimal subtotal) {

        long referrals = getSuccessfulReferrals(userId);

        if (referrals < TARGET_REFERRALS) {
            return BigDecimal.ZERO;
        }

        return subtotal.multiply(DISCOUNT_PERCENT);
    }

    @Override
    public BigDecimal getAvailableReferralDiscountPercentForUser(
            Long userId) {

        long referrals = getSuccessfulReferrals(userId);

        if (referrals >= TARGET_REFERRALS) {
            return DISCOUNT_PERCENT;
        }

        return BigDecimal.ZERO;
    }

    @Override
    public void markReferralDiscountUsed(
            Long userId,
            Long orderId) {

        ReferralOrderDiscountRedemption redemption =
                new ReferralOrderDiscountRedemption();

        redemption.setUserId(userId);
        redemption.setOrderId(orderId);
        redemption.setDiscountAmount(BigDecimal.ZERO);
        redemption.setCartSubtotal(BigDecimal.ZERO);

        discountRepository.save(redemption);
    }

    @Override
    public void confirmReferralOnFirstPaidOrder(Long userId) {
        // optional future use
    }

    @Override
    public ReferralDashboardDto getDashboard(Long userId) {

        User user = userRepository.findById(userId).orElseThrow();

        long invites = getSuccessfulReferrals(userId);

        long remaining = TARGET_REFERRALS - invites;

        if (remaining < 0) {
            remaining = 0;
        }

        return new ReferralDashboardDto(
                user.getReferralCode(),
                invites,
                TARGET_REFERRALS,
                remaining,
                invites >= TARGET_REFERRALS,
                "10% first order discount"
        );
    }

    @Override
    public String refreshReferralCode(Long userId) {

        User user = userRepository.findById(userId).orElseThrow();

        String newCode =
                "FNT" + userId +
                        (char) (65 + new Random().nextInt(26));

        user.setReferralCode(newCode);

        userRepository.save(user);

        return newCode;
    }

    @Override
    public ShareDto getShareData(Long userId) {

        User user = userRepository.findById(userId).orElseThrow();

        return new ShareDto(
                "Join Flintnthread using my code "
                        + user.getReferralCode(),

                "https://flintnthread.com/register?ref="
                        + user.getReferralCode()
        );
    }

    private long getSuccessfulReferrals(Long userId) {

        return transactionRepository
                .countByReferrerIdAndStatus(
                        userId,
                        ReferralTransaction.Status.completed
                );
    }
}