package com.ecommerce.authdemo.service;


import com.ecommerce.authdemo.dto.CheckoutRequest;
import com.ecommerce.authdemo.dto.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

    public interface OrderService {

        OrderResponse placeOrder(Long userId, CheckoutRequest request);

        OrderResponse getOrder(String orderNumber);

        Page<OrderResponse> getUserOrders(Long userId, Pageable pageable);

        void cancelOrder(String orderNumber);
    }

