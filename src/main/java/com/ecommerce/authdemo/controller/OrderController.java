package com.ecommerce.authdemo.controller;


import com.ecommerce.authdemo.dto.CheckoutRequest;
import com.ecommerce.authdemo.dto.OrderResponse;
import com.ecommerce.authdemo.service.OrderService;
import lombok.RequiredArgsConstructor;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.web.bind.annotation.*;

    @RestController
    @RequestMapping("/api/orders")
    @RequiredArgsConstructor
    public class OrderController {

        private final OrderService orderService;

        // Checkout and place order
        @PostMapping("/{userId}/checkout")
        public OrderResponse placeOrder(
                @PathVariable Long userId,
                @RequestBody CheckoutRequest request) {

            return orderService.placeOrder(userId, request);
        }

        // Get order details
        @GetMapping("/{orderNumber}")
        public OrderResponse getOrder(
                @PathVariable String orderNumber) {

            return orderService.getOrder(orderNumber);
        }

        // Get all orders of a user
        @GetMapping("/user/{userId}")
        public Page<OrderResponse> getUserOrders(
                @PathVariable Long userId,
                Pageable pageable) {

            return orderService.getUserOrders(userId, pageable);
        }

        // Cancel order
        @PutMapping("/cancel/{orderNumber}")
        public void cancelOrder(
                @PathVariable String orderNumber) {

            orderService.cancelOrder(orderNumber);
        }
    }

