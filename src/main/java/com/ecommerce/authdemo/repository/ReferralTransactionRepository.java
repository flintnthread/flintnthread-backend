package com.ecommerce.authdemo.repository;



import com.ecommerce.authdemo.entity.ReferralTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

    public interface ReferralTransactionRepository extends JpaRepository<ReferralTransaction, Integer> {

        List<ReferralTransaction> findByReferrerId(Integer referrerId);

        List<ReferralTransaction> findByReferredUserId(Integer referredUserId);

        List<ReferralTransaction> findByStatus(ReferralTransaction.Status status);
    }

