package com.ecommerce.authdemo.repository;


import com.ecommerce.authdemo.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

    public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Integer> {

        List<PaymentTransaction> findByOrderId(Integer orderId);

        PaymentTransaction findByTransactionId(String transactionId);
    }

