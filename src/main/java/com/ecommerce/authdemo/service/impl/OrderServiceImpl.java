package com.ecommerce.authdemo.service.impl;



import com.ecommerce.authdemo.dto.CheckoutRequest;
import com.ecommerce.authdemo.dto.Enum.OrderStatus;
import com.ecommerce.authdemo.dto.OrderItemResponse;
import com.ecommerce.authdemo.dto.OrderResponse;
import com.ecommerce.authdemo.entity.Cart;
import com.ecommerce.authdemo.entity.Order;
import com.ecommerce.authdemo.entity.OrderItem;
import com.ecommerce.authdemo.entity.OrderStatusHistory;
import com.ecommerce.authdemo.repository.AddressRepository;
import com.ecommerce.authdemo.repository.OrderItemRepository;
import com.ecommerce.authdemo.repository.OrderRepository;
import com.ecommerce.authdemo.repository.OrderStatusHistoryRepository;
import com.ecommerce.authdemo.service.CartService;
import com.ecommerce.authdemo.service.OrderService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

    @Service
    @RequiredArgsConstructor
    @Transactional
    public class OrderServiceImpl implements OrderService {

        private final CartService cartService;
        private final OrderRepository orderRepository;
        private final OrderItemRepository orderItemRepository;
        private final OrderStatusHistoryRepository statusHistoryRepository;
        private final AddressRepository addressRepository;

        @Override
        public OrderResponse placeOrder(Long userId, CheckoutRequest request) {

            Cart cart = cartService.getCart(userId);

            if (cart.getItems().isEmpty())
                throw new RuntimeException("Cart is empty");

            Order order = new Order();

            order.setUserId(userId);
            order.setOrderNumber(generateOrderNumber());
            order.setAddressId(request.getAddressId());
            order.setStatus(OrderStatus.PLACED);

            order.setTotalAmount(cart.getTotalAmount());
            order.setFinalAmount(cart.getFinalAmount());
            order.setPaymentMethod(request.getPaymentMethod());
            order.setPaymentCompleted(false);

            order = orderRepository.save(order);

            Order finalOrder = order;
            List<OrderItem> orderItems = cart.getItems().stream().map(cartItem -> {

                OrderItem item = new OrderItem();

                item.setOrder(finalOrder);
                item.setProductId(cartItem.getProductId());
                item.setSellerId(cartItem.getSellerId());
                item.setQuantity(cartItem.getQuantity());
                item.setUnitPrice(cartItem.getUnitPrice());
                item.setTotal(cartItem.getTotalPrice());

                return item;

            }).collect(Collectors.toList());

            orderItemRepository.saveAll(orderItems);

            saveOrderStatus(order.getId(), OrderStatus.PLACED);

            cartService.clearCart(userId);

            return mapToResponse(order, orderItems);
        }

        private void saveOrderStatus(Long orderId, OrderStatus status) {

            OrderStatusHistory history = new OrderStatusHistory();

            history.setId(orderId);
            history.setStatus(status);

            statusHistoryRepository.save(history);
        }

        private String generateOrderNumber() {

            return "ORD-" + UUID.randomUUID().toString().substring(0, 8);
        }

        @Override
        public OrderResponse getOrder(String orderNumber) {

            Order order = orderRepository.findByOrderNumber(orderNumber)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());

            return mapToResponse(order, items);
        }

        @Override
        public Page<OrderResponse> getUserOrders(Long userId, Pageable pageable) {

            return orderRepository.findByUserId(userId, pageable)
                    .map(order -> {

                        List<OrderItem> items =
                                orderItemRepository.findByOrderId(order.getId());

                        return mapToResponse(order, items);
                    });
        }

        @Override
        public void cancelOrder(String orderNumber) {

            Order order = orderRepository.findByOrderNumber(orderNumber)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            order.setStatus(OrderStatus.CANCELLED);

            orderRepository.save(order);

            saveOrderStatus(order.getId(), OrderStatus.CANCELLED);
        }

        private OrderResponse mapToResponse(Order order, List<OrderItem> items) {

            OrderResponse response = new OrderResponse();

            response.setOrderNumber(order.getOrderNumber());
            response.setStatus(order.getStatus().name());
            response.setTotalAmount(order.getTotalAmount());
            response.setFinalAmount(order.getFinalAmount());

            List<OrderItemResponse> itemResponses = items.stream().map(item -> {

                OrderItemResponse res = new OrderItemResponse();

                res.setProductId(item.getProductId());
                res.setQuantity(item.getQuantity());
                res.setPrice(item.getUnitPrice());

                return res;

            }).collect(Collectors.toList());

            response.setItems(itemResponses);

            return response;
        }
    }

