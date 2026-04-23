package com.ecommerce.authdemo.service.impl;



import com.ecommerce.authdemo.entity.UserWallet;
import com.ecommerce.authdemo.entity.WalletTransaction;
import com.ecommerce.authdemo.dto.WalletResponse;
import com.ecommerce.authdemo.repository.UserWalletRepository;
import com.ecommerce.authdemo.repository.WalletTransactionRepository;
import com.ecommerce.authdemo.exception.ResourceNotFoundException;
import com.ecommerce.authdemo.service.WalletService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

    @Service
    @RequiredArgsConstructor
    public class WalletServiceImpl implements WalletService {

        private final UserWalletRepository walletRepo;
        private final WalletTransactionRepository walletTransactionRepo;

        // ✅ Create wallet on signup
        @Override
        public void createWallet(Integer userId) {

            UserWallet wallet = UserWallet.builder()
                    .userId(userId)
                    .balance(BigDecimal.ZERO)
                    .totalEarned(BigDecimal.ZERO)
                    .totalSpent(BigDecimal.ZERO)
                    .build();

            walletRepo.save(wallet);
        }

        @Override
        public WalletResponse getWallet(Integer userId) {
            UserWallet wallet = walletRepo.findByUserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
            return toResponse(wallet);
        }

        // ✅ Add money (referral / cashback)
        @Override
        public void addMoney(Integer userId, Double amount) {

            UserWallet wallet = walletRepo.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Wallet not found"));

            wallet.setBalance(wallet.getBalance().add(BigDecimal.valueOf(amount)));
            wallet.setTotalEarned(wallet.getTotalEarned().add(BigDecimal.valueOf(amount)));

            walletRepo.save(wallet);

            // ✅ SAVE TRANSACTION
            WalletTransaction txn = WalletTransaction.builder()
                    .sellerId(userId) // or userId (rename later if needed)
                    .amount(BigDecimal.valueOf(amount))
                    .type(WalletTransaction.Type.credit)
                    .description("Wallet credited")
                    .createdBy(userId)
                    .build();

            walletTransactionRepo.save(txn);
        }

        // ✅ Deduct money (order usage)
        @Override
        public void deductMoney(Integer userId, Double amount) {

            UserWallet wallet = walletRepo.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Wallet not found"));

            if (wallet.getBalance().compareTo(BigDecimal.valueOf(amount)) < 0) {
                throw new RuntimeException("Insufficient wallet balance");
            }

            wallet.setBalance(wallet.getBalance().subtract(BigDecimal.valueOf(amount)));
            wallet.setTotalSpent(wallet.getTotalSpent().add(BigDecimal.valueOf(amount)));

            walletRepo.save(wallet);

            // ✅ SAVE TRANSACTION
            WalletTransaction txn = WalletTransaction.builder()
                    .sellerId(userId)
                    .amount(BigDecimal.valueOf(amount))
                    .type(WalletTransaction.Type.debit)
                    .description("Wallet debited for order")
                    .createdBy(userId)
                    .build();

            walletTransactionRepo.save(txn);
        }

        private WalletResponse toResponse(UserWallet wallet) {
            return WalletResponse.builder()
                    .id(wallet.getId())
                    .userId(wallet.getUserId())
                    .balance(wallet.getBalance())
                    .totalEarned(wallet.getTotalEarned())
                    .totalSpent(wallet.getTotalSpent())
                    .createdAt(wallet.getCreatedAt())
                    .updatedAt(wallet.getUpdatedAt())
                    .build();
        }
    }

