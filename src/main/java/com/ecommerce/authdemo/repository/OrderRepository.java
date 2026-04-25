package com.ecommerce.authdemo.repository;

import com.ecommerce.authdemo.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Order> findByUserIdAndOrderStatusOrderByCreatedAtDesc(Long userId, String status);

    Optional<Order> findByRazorpayOrderId(String razorpayOrderId);
    List<Order> findByRazorpayOrderIdOrderByCreatedAtDesc(String razorpayOrderId);

    Optional<Order> findByOrderNumber(String orderNumber);

    Optional<Order> findByShiprocketAwbCode(String shiprocketAwbCode);

    Optional<Order> findTopByPaymentStatusOrderByCreatedAtDesc(String status);

    Optional<Order> findTopByUserIdAndPaymentStatusOrderByCreatedAtDesc(Long userId, String paymentStatus);
}