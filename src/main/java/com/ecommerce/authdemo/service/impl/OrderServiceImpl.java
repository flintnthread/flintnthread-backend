package com.ecommerce.authdemo.service.impl;

import com.ecommerce.authdemo.dto.*;
import com.ecommerce.authdemo.entity.*;
import com.ecommerce.authdemo.repository.AddressRepository;
import com.ecommerce.authdemo.repository.OrderItemRepository;
import com.ecommerce.authdemo.repository.OrderRepository;
import com.ecommerce.authdemo.service.CartService;
import com.ecommerce.authdemo.service.OrderService;
import com.ecommerce.authdemo.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartService cartService;
    private final AddressRepository addressRepository;
    private final SecurityUtil securityUtil;

    @Override
    @Transactional
    public OrderResponseDTO placeOrder(PlaceOrderRequestDTO dto) {

        Long userId = securityUtil.getCurrentUserId();
        CartResponseDTO cart = cartService.getCart();

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Address address = addressRepository.findById(Math.toIntExact(dto.getAddressId()))
                .orElseThrow(() -> new RuntimeException("Address not found"));

        double totalAmount = 0;

        for (CartItemResponseDTO item : cart.getItems()) {
            totalAmount += item.getPrice().doubleValue() * item.getQuantity();
        }

        double deliveryCharge = 50;
        double discount = 0;
        double finalAmount = totalAmount + deliveryCharge - discount;

        Order order = new Order();

        order.setUserId(userId);
        order.setOrderNumber("ORD" + System.currentTimeMillis());

        order.setTotalAmount(totalAmount);
        order.setShippingAmount(deliveryCharge);
        order.setDiscountAmount(discount);
        order.setFinalAmount(finalAmount);

        order.setPaymentMethod(dto.getPaymentMethod());
        order.setPaymentStatus("pending");
        order.setOrderStatus("pending");

        order.setShippingName(address.getName());
        order.setShippingPhone(address.getPhone());
        order.setShippingAddress1(address.getAddressLine1());
        order.setShippingCity(address.getCity());
        order.setShippingState(address.getState());
        order.setShippingPincode(address.getPincode());

        order.setAddressId(Long.valueOf(address.getId()));
        order.setCreatedAt(LocalDateTime.now());

        order = orderRepository.save(order);

        List<OrderItemDTO> itemDTOList = new ArrayList<>();

        for (CartItemResponseDTO cartItem : cart.getItems()) {

            OrderItem item = new OrderItem();

            item.setOrderId(order.getId());
            item.setProductId(cartItem.getProductId());

            item.setVariantId(
                    cartItem.getVariantId() != null ? cartItem.getVariantId() : null
            );

            item.setQuantity(cartItem.getQuantity());

            BigDecimal price = cartItem.getPrice();
            BigDecimal total = price.multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            item.setPrice(price.doubleValue());
            item.setTotal(total.doubleValue());

            item.setStatus("pending");
            item.setProductImagePath(cartItem.getImage());

            orderItemRepository.save(item);

            itemDTOList.add(OrderItemDTO.builder()
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .price(item.getPrice())
                    .total(item.getTotal())
                    .productImage(item.getProductImagePath())
                    .build());
        }

        cartService.clearCart();

        return OrderResponseDTO.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .totalAmount(order.getTotalAmount())
                .finalAmount(order.getFinalAmount())
                .paymentStatus(order.getPaymentStatus())
                .orderStatus(order.getOrderStatus())
                .items(itemDTOList)
                .build();
    }

    @Override
    public List<OrderResponseDTO> getUserOrders() {

        Long userId = securityUtil.getCurrentUserId();

        List<Order> orders = orderRepository.findByUserId(userId);

        return orders.stream()
                .map(o -> OrderResponseDTO.builder()
                        .orderId(o.getId())
                        .orderNumber(o.getOrderNumber())
                        .finalAmount(o.getFinalAmount())
                        .orderStatus(o.getOrderStatus())
                        .paymentStatus(o.getPaymentStatus())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponseDTO getOrderDetails(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

        return OrderResponseDTO.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .finalAmount(order.getFinalAmount())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .items(
                        items.stream().map(i ->
                                OrderItemDTO.builder()
                                        .productId(i.getProductId())
                                        .quantity(i.getQuantity())
                                        .price(i.getPrice())
                                        .total(i.getTotal())
                                        .productImage(i.getProductImagePath())
                                        .build()
                        ).collect(Collectors.toList()) // ✅ FIXED
                )
                .build();
    }
}