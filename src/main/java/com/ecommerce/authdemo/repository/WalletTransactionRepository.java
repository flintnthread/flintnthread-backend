package com.ecommerce.authdemo.repository;





import com.ecommerce.authdemo.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

    public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Integer> {

        List<WalletTransaction> findBySellerId(Integer sellerId);

        List<WalletTransaction> findByOrderId(Integer orderId);

        List<WalletTransaction> findBySellerIdAndOrderId(Integer sellerId, Integer orderId);
    }

