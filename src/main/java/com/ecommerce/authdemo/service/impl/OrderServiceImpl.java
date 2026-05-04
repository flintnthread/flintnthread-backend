package com.ecommerce.authdemo.service.impl;

import com.ecommerce.authdemo.dto.*;
import com.ecommerce.authdemo.entity.*;
import com.ecommerce.authdemo.exception.ResourceNotFoundException;
import com.ecommerce.authdemo.exception.OrderException;
import com.ecommerce.authdemo.repository.*;
import com.ecommerce.authdemo.service.CartService;
import com.ecommerce.authdemo.service.OrderService;
import com.ecommerce.authdemo.service.ReferralService;
import com.ecommerce.authdemo.util.SecurityUtil;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartService cartService;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final SecurityUtil securityUtil;
    private final ReferralService referralService;

    @Override
    @Transactional
    public OrderResponseDTO placeOrder(PlaceOrderRequestDTO dto) {

        try {
            validatePlaceOrderRequest(dto);

            Long userId = securityUtil.getCurrentUserId();
            CartResponseDTO cart = cartService.getCart();

            if (cart.getItems() == null || cart.getItems().isEmpty()) {
                throw new OrderException("Cart is empty");
            }

            // ✅ FINAL STOCK VALIDATION + DECREMENT (atomic)
            // If any item cannot be decremented (insufficient stock), we fail and the whole transaction rolls back.
            for (CartItemResponseDTO item : cart.getItems()) {
                int updated = productVariantRepository.decrementStockIfAvailable(
                        item.getVariantId(),
                        item.getQuantity() != null ? item.getQuantity() : 0
                );
                if (updated == 0) {
                    Integer current = productVariantRepository.findStockByVariantId(item.getVariantId()).orElse(0);
                    throw new OrderException("Only " + current + " items available");
                }
            }

            Address address = addressRepository.findById(Math.toIntExact(dto.getAddressId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

            BigDecimal subtotal = cart.getPriceSummary().getSubtotal();
            BigDecimal shipping = cart.getPriceSummary().getDeliveryCharge();
            BigDecimal discount = cart.getPriceSummary().getDiscount();
            BigDecimal finalAmount = cart.getPriceSummary().getFinalTotal();

            // ✅ Apply referral discount if applicable
            boolean referralDiscountApplied = false;
            double referralDiscountPercent = 0.0d;

            try {
                long paidOrders = orderRepository.countByUserIdAndPaymentStatus(userId, "paid");

                if (paidOrders == 0) {
                    BigDecimal discountPercent = referralService
                            .getAvailableReferralDiscountPercentForUser(userId);

                    referralDiscountPercent = (discountPercent != null)
                            ? discountPercent.doubleValue()
                            : 0.0d;
                }

            } catch (Exception e) {
                log.warn("[ORDER] referral discount lookup failed userId={}: {}", userId, e.getMessage());
            }

            if (referralDiscountPercent > 0) {
                BigDecimal extraDiscount = subtotal
                        .multiply(BigDecimal.valueOf(referralDiscountPercent))
                        .divide(BigDecimal.valueOf(100));
                discount = discount.add(extraDiscount);
                finalAmount = subtotal.add(shipping).subtract(discount).max(BigDecimal.ZERO);
                referralDiscountApplied = true;
            }

            Order order = createOrder(userId, dto, address, subtotal, shipping, discount, finalAmount);
            order = orderRepository.save(order);

            List<OrderItemDTO> itemDTOList = createOrderItems(order, cart.getItems());

            clearCartSafely(userId);

            return buildOrderResponse(order, itemDTOList);

        } catch (OptimisticLockException e) {
            throw new OrderException("Stock updated by another user. Please try again.");
        }
    }

    @Override
    public List<OrderResponseDTO> getUserOrders(String status) {
        Long userId = securityUtil.getCurrentUserId();
        List<Order> orders;

        if (status == null || status.equalsIgnoreCase("all")) {
            orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        } else {
            orders = orderRepository.findByUserIdAndOrderStatusOrderByCreatedAtDesc(userId, status);
        }

        return orders.stream().map(this::buildOrderSummaryResponse).toList();
    }

    @Override
    public OrderResponseDTO getOrderDetails(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        List<OrderItemDTO> itemDTOList = items.stream().map(this::buildOrderItemDTO).toList();

        return buildDetailedOrderResponse(order, itemDTOList);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // ✅ Prevent double cancel
        if ("cancelled".equalsIgnoreCase(order.getOrderStatus())) {
            throw new OrderException("Order already cancelled");
        }

        // ✅ Restore stock
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

        for (OrderItem item : items) {

            ProductVariant variant = productVariantRepository.findById(item.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found"));

            variant.setStock(variant.getStock() + item.getQuantity());
            productVariantRepository.save(variant);
        }

        order.setOrderStatus("cancelled");
        orderRepository.save(order);
    }

    @Override
    public Order markOrderAsPaid(String razorpayOrderId, String paymentId) {
        return null;
    }

    @Override
    public void updateShipment(String orderNumber, String awb, String courier, String trackingUrl, String shiprocketStatus) {

    }

    @Override
    public void markShiprocketCreateFailed(String orderNumber, String reason) {

    }

    private Order createOrder(Long userId, PlaceOrderRequestDTO dto, Address address,
                              BigDecimal subtotal, BigDecimal shipping,
                              BigDecimal discount, BigDecimal finalAmount) {

        return Order.builder()
                .userId(userId)
                .orderNumber("ORD-" + System.currentTimeMillis())
                .totalAmount(finalAmount.doubleValue())
                .shippingAmount(shipping.doubleValue())
                .discountAmount(discount.doubleValue())
                .paymentMethod(dto.getPaymentMethod())
                .paymentStatus("pending")
                .orderStatus("processing")
                .shippingAddress1(address.getAddressLine1())
                .shippingCity(address.getCity())
                .shippingState(address.getState())
                .shippingPincode(address.getPincode())
                .createdAt(LocalDateTime.now())
                .build();
    }

    private void clearCartSafely(Long userId) {
        cartRepository.deleteByUser_Id(userId);
    }

    private List<OrderItemDTO> createOrderItems(Order order, List<CartItemResponseDTO> cartItems) {

        List<OrderItemDTO> list = new ArrayList<>();

        for (CartItemResponseDTO cartItem : cartItems) {

            OrderItem item = OrderItem.builder()
                    .orderId(order.getId())
                    .productId(cartItem.getProductId())
                    .variantId(cartItem.getVariantId())
                    .quantity(cartItem.getQuantity())
                    .price(cartItem.getPrice().doubleValue())
                    .total(cartItem.getTotal().doubleValue())
                    .status("processing")
                    .build();

            orderItemRepository.save(item);
            list.add(buildOrderItemDTO(item));
        }

        return list;
    }

    private OrderItemDTO buildOrderItemDTO(OrderItem item) {
        return OrderItemDTO.builder()
                .productId(item.getProductId())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .total(item.getTotal())
                .build();
    }

    private OrderResponseDTO buildOrderResponse(Order order, List<OrderItemDTO> items) {
        return OrderResponseDTO.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderStatus(order.getOrderStatus())
                .items(items)
                .build();
    }

    private OrderResponseDTO buildOrderSummaryResponse(Order order) {
        return OrderResponseDTO.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderStatus(order.getOrderStatus())
                .build();
    }

    private OrderResponseDTO buildDetailedOrderResponse(Order order, List<OrderItemDTO> items) {
        return OrderResponseDTO.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .items(items)
                .build();
    }

    private void validatePlaceOrderRequest(PlaceOrderRequestDTO dto) {
        if (dto == null || dto.getAddressId() == null) {
            throw new OrderException("Invalid request");
        }
    }

    @Override
    @Transactional
    public void linkRazorpayOrder(String razorpayOrderId) {
        // TODO: Implement Razorpay order linking logic
        log.info("Linking Razorpay order: {}", razorpayOrderId);
    }

    @Override
    @Transactional
    public void updateOrderStatusFromWebhook(String awb, String status) {
        // TODO: Implement webhook status update logic
        log.info("Updating order status from webhook: awb={}, status={}", awb, status);
    }
}