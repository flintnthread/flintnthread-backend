package com.ecommerce.authdemo.service.impl;



import com.ecommerce.authdemo.entity.ReferralTransaction;
import com.ecommerce.authdemo.repository.ReferralTransactionRepository;
import com.ecommerce.authdemo.service.ReferralService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

    @Service
    @RequiredArgsConstructor
    public class ReferralServiceImpl implements ReferralService {

        private final ReferralTransactionRepository referralTransactionRepo;

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
    }

