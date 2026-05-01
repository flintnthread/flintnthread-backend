package com.ecommerce.authdemo.repository;



import com.ecommerce.authdemo.entity.ReferralTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

    public interface ReferralTransactionRepository extends JpaRepository<ReferralTransaction, Integer> {

        List<ReferralTransaction> findByReferrerId(Integer referrerId);

        List<ReferralTransaction> findByReferredUserId(Integer referredUserId);

        List<ReferralTransaction> findByStatus(ReferralTransaction.Status status);

        long countByReferrerIdAndTransactionTypeAndStatus(
                Integer referrerId,
                ReferralTransaction.TransactionType transactionType,
                ReferralTransaction.Status status
        );

        boolean existsByReferrerIdAndReferredUserIdAndTransactionType(
                Integer referrerId,
                Integer referredUserId,
                ReferralTransaction.TransactionType transactionType
        );

        Optional<ReferralTransaction> findTopByReferredUserIdAndTransactionTypeAndStatusOrderByCreatedAtDesc(
                Integer referredUserId,
                ReferralTransaction.TransactionType transactionType,
                ReferralTransaction.Status status
        );
    }

