package com.ecommerce.authdemo.service.impl;



import com.ecommerce.authdemo.dto.ReferralOverviewDTO;
import com.ecommerce.authdemo.entity.ReferralOrderDiscountRedemption;
import com.ecommerce.authdemo.entity.ReferralSetting;
import com.ecommerce.authdemo.entity.ReferralTransaction;
import com.ecommerce.authdemo.exception.OrderException;
import com.ecommerce.authdemo.repository.OrderRepository;
import com.ecommerce.authdemo.repository.ReferralOrderDiscountRedemptionRepository;
import com.ecommerce.authdemo.repository.ReferralSettingRepository;
import com.ecommerce.authdemo.repository.ReferralTransactionRepository;
import com.ecommerce.authdemo.service.ReferralService;
import com.ecommerce.authdemo.util.SecurityUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReferralServiceImpl implements ReferralService {

    private final ReferralTransactionRepository referralTransactionRepo;
    private final ReferralSettingRepository referralSettingRepository;
    private final ReferralOrderDiscountRedemptionRepository redemptionRepository;
    private final OrderRepository orderRepository;
    private final SecurityUtil securityUtil;

    private static final int DEFAULT_REQUIRED_REFERRALS = 5;
    private static final double DEFAULT_DISCOUNT_PERCENT = 10.0d;
    private static final String REFERRAL_CODE_PREFIX = "FNT";

    @Override
    public void giveSignupBonus(Integer referrerId, Integer newUserId) {

        ReferralTransaction rt = ReferralTransaction.builder()
                .referrerId(referrerId)
                .referredUserId(newUserId)
                .amount(BigDecimal.valueOf(100))
                .transactionType(ReferralTransaction.TransactionType.signup_bonus)
                .status(ReferralTransaction.Status.completed)
                .description("Signup bonus credited")
                .build();

        referralTransactionRepo.save(rt);
    }

    @Override
        @Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
    public ReferralOverviewDTO getMyReferralOverview() {
            try {
                Long userId = securityUtil.getCurrentUserId();
                return buildOverviewForUser(userId);
            } catch (Exception e) {
                // Never fail overview endpoint; return safe defaults.
                Long fallbackUserId = 1L;
                try {
                    fallbackUserId = securityUtil.tryGetCurrentUserId().orElse(1L);
                } catch (Exception ignored) {
                    fallbackUserId = 1L;
                }
                return ReferralOverviewDTO.builder()
                        .referralCode(encodeReferralCode(fallbackUserId))
                        .confirmedReferrals(0)
                        .requiredReferrals(DEFAULT_REQUIRED_REFERRALS)
                        .discountPercent(DEFAULT_DISCOUNT_PERCENT)
                        .rewardUnlocked(false)
                        .rewardUsed(false)
                        .build();
            }
    }

    @Override
    @Transactional
    public ReferralOverviewDTO applyReferralCode(String referralCode) {
        Long currentUserId = securityUtil.getCurrentUserId();
        long referrerUserId = decodeReferralCode(referralCode);

        if (currentUserId.equals(referrerUserId)) {
            throw new OrderException("You cannot apply your own referral code.");
        }

        boolean alreadyApplied = referralTransactionRepo.existsByReferrerIdAndReferredUserIdAndTransactionType(
                Math.toIntExact(referrerUserId),
                Math.toIntExact(currentUserId),
                ReferralTransaction.TransactionType.referral_bonus
        );
        if (alreadyApplied) {
            throw new OrderException("Referral code already applied.");
        }

        ReferralTransaction tx = ReferralTransaction.builder()
                .referrerId(Math.toIntExact(referrerUserId))
                .referredUserId(Math.toIntExact(currentUserId))
                .amount(BigDecimal.ZERO)
                .transactionType(ReferralTransaction.TransactionType.referral_bonus)
                .status(ReferralTransaction.Status.pending)
                .description("Referral code applied; pending first paid order confirmation")
                .build();
        referralTransactionRepo.save(tx);

        // Return applicant's overview (not referrer's) and never fail apply due to overview read issues.
        try {
            return buildOverviewForUser(currentUserId);
        } catch (Exception ignored) {
            return ReferralOverviewDTO.builder()
                    .referralCode(encodeReferralCode(currentUserId))
                    .confirmedReferrals(0)
                    .requiredReferrals(DEFAULT_REQUIRED_REFERRALS)
                    .discountPercent(DEFAULT_DISCOUNT_PERCENT)
                    .rewardUnlocked(false)
                    .rewardUsed(false)
                    .build();
        }
    }

    @Override
    @Transactional
    public void confirmReferralOnFirstPaidOrder(Long referredUserId) {
        Optional<ReferralTransaction> pendingTx = referralTransactionRepo
                .findTopByReferredUserIdAndTransactionTypeAndStatusOrderByCreatedAtDesc(
                        Math.toIntExact(referredUserId),
                        ReferralTransaction.TransactionType.referral_bonus,
                        ReferralTransaction.Status.pending
                );
        if (pendingTx.isEmpty()) {
            return;
        }

        ReferralTransaction tx = pendingTx.get();
        tx.setStatus(ReferralTransaction.Status.completed);
        tx.setDescription("Referral confirmed on first paid order");
        referralTransactionRepo.save(tx);

        ReferralRules rules = resolveRules();
        long confirmedCount = referralTransactionRepo.countByReferrerIdAndTransactionTypeAndStatus(
                tx.getReferrerId(),
                ReferralTransaction.TransactionType.referral_bonus,
                ReferralTransaction.Status.completed
        );

        if (confirmedCount < rules.requiredReferrals()) {
            return;
        }

        boolean hasUnlocked = redemptionRepository
                .findTopByReferrerUserIdOrderByCreatedAtDesc(Long.valueOf(tx.getReferrerId()))
                .isPresent();
        if (hasUnlocked) {
            return;
        }

        ReferralOrderDiscountRedemption redemption = ReferralOrderDiscountRedemption.builder()
                .referrerUserId(Long.valueOf(tx.getReferrerId()))
                .requiredReferrals(rules.requiredReferrals())
                .referralCountAtUnlock((int) confirmedCount)
                .discountPercent(rules.discountPercent())
                .used(false)
                .usedOrderId(null)
                .usedAt(null)
                .build();
        redemptionRepository.save(redemption);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
    public double getAvailableReferralDiscountPercentForUser(Long userId) {
        try {
            Optional<ReferralOrderDiscountRedemption> latest = redemptionRepository
                    .findTopByReferrerUserIdOrderByCreatedAtDesc(userId);
            if (latest.isPresent()) {
                ReferralOrderDiscountRedemption r = latest.get();
                if (Boolean.TRUE.equals(r.getUsed())) return 0.0d;
                return r.getDiscountPercent() != null ? r.getDiscountPercent() : DEFAULT_DISCOUNT_PERCENT;
            }
        } catch (Exception ignored) {
            // Continue with transaction-count based fallback.
        }

        try {
            // Backfill eligibility even if unlock row is missing.
            ReferralRules rules = resolveRules();
            long confirmedCount = referralTransactionRepo.countByReferrerIdAndTransactionTypeAndStatus(
                    Math.toIntExact(userId),
                    ReferralTransaction.TransactionType.referral_bonus,
                    ReferralTransaction.Status.completed
            );
            return confirmedCount >= rules.requiredReferrals() ? rules.discountPercent() : 0.0d;
        } catch (Exception ignored) {
            return 0.0d;
        }
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void markReferralDiscountUsed(Long userId, Long orderId) {
        ReferralRules rules = resolveRules();
        long confirmedCount = referralTransactionRepo.countByReferrerIdAndTransactionTypeAndStatus(
                Math.toIntExact(userId),
                ReferralTransaction.TransactionType.referral_bonus,
                ReferralTransaction.Status.completed
        );
        if (confirmedCount < rules.requiredReferrals()) {
            return;
        }

        ReferralOrderDiscountRedemption redemption = redemptionRepository
                .findTopByReferrerUserIdAndUsedFalseOrderByCreatedAtDesc(userId)
                .orElseGet(() -> redemptionRepository.save(
                        ReferralOrderDiscountRedemption.builder()
                                .referrerUserId(userId)
                                .requiredReferrals(rules.requiredReferrals())
                                .referralCountAtUnlock((int) confirmedCount)
                                .discountPercent(rules.discountPercent())
                                .used(false)
                                .usedOrderId(null)
                                .usedAt(null)
                                .build()
                ));

        redemption.setUsed(true);
        redemption.setUsedOrderId(orderId);
        redemption.setUsedAt(LocalDateTime.now());
        redemptionRepository.save(redemption);
    }

    private ReferralOverviewDTO buildOverviewForUser(Long userId) {
        ReferralRules rules = resolveRules();
            int confirmedReferrals = 0;
            try {
                confirmedReferrals = (int) referralTransactionRepo.countByReferrerIdAndTransactionTypeAndStatus(
                        Math.toIntExact(userId),
                        ReferralTransaction.TransactionType.referral_bonus,
                        ReferralTransaction.Status.completed
                );
            } catch (Exception ignored) {
                confirmedReferrals = 0;
            }
        boolean rewardUnlocked = confirmedReferrals >= rules.requiredReferrals();
        boolean rewardUsed = false;
        try {
            // Treat reward as used if any redemption row is marked used.
            rewardUsed = redemptionRepository.existsByReferrerUserIdAndUsedTrue(userId);
        } catch (Exception ignored) {
            rewardUsed = false;
        }
        if (!rewardUsed) {
            try {
                // Fallback: if any user order has positive discount, reward is considered consumed.
                rewardUsed = orderRepository.countByUserIdAndDiscountAmountGreaterThan(userId, 0.0d) > 0;
            } catch (Exception ignored) {
                // keep current value
            }
        }

        return ReferralOverviewDTO.builder()
                .referralCode(encodeReferralCode(userId))
                .confirmedReferrals(confirmedReferrals)
                .requiredReferrals(rules.requiredReferrals())
                .discountPercent(rules.discountPercent())
                .rewardUnlocked(rewardUnlocked)
                .rewardUsed(rewardUsed)
                .build();
    }

    private ReferralRules resolveRules() {
            try {
                return referralSettingRepository.findTopByActiveTrueOrderByIdDesc()
                        .map(s -> new ReferralRules(
                                sanitizeRequiredReferrals(s),
                                sanitizeDiscountPercent(s)
                        ))
                        .orElse(new ReferralRules(DEFAULT_REQUIRED_REFERRALS, DEFAULT_DISCOUNT_PERCENT));
            } catch (Exception ignored) {
                return new ReferralRules(DEFAULT_REQUIRED_REFERRALS, DEFAULT_DISCOUNT_PERCENT);
            }
    }

    private int sanitizeRequiredReferrals(ReferralSetting s) {
        Integer v = s.getRequiredReferrals();
        return v != null && v > 0 ? v : DEFAULT_REQUIRED_REFERRALS;
    }

    private double sanitizeDiscountPercent(ReferralSetting s) {
        Double v = s.getDiscountPercent();
        if (v == null || v <= 0) return DEFAULT_DISCOUNT_PERCENT;
        return Math.min(100.0d, v);
    }

    private String encodeReferralCode(Long userId) {
        return REFERRAL_CODE_PREFIX + Long.toString(userId, 36).toUpperCase();
    }

    private long decodeReferralCode(String rawCode) {
        String code = rawCode == null ? "" : rawCode.trim().toUpperCase();
        if (!code.startsWith(REFERRAL_CODE_PREFIX) || code.length() <= REFERRAL_CODE_PREFIX.length()) {
            throw new OrderException("Invalid referral code.");
        }
        String encoded = code.substring(REFERRAL_CODE_PREFIX.length());
        try {
            long userId = Long.parseLong(encoded, 36);
            if (userId <= 0) throw new NumberFormatException("non-positive");
            return userId;
        } catch (NumberFormatException ex) {
            throw new OrderException("Invalid referral code.");
        }
    }

    private static final class ReferralRules {
        private final int requiredReferrals;
        private final double discountPercent;

        private ReferralRules(int requiredReferrals, double discountPercent) {
            this.requiredReferrals = requiredReferrals;
            this.discountPercent = discountPercent;
        }

        int requiredReferrals() {
            return requiredReferrals;
        }

        double discountPercent() {
            return discountPercent;
        }
    }
}

