package com.ecommerce.authdemo.service.impl;

import com.ecommerce.authdemo.dto.*;
import com.ecommerce.authdemo.entity.*;
import com.ecommerce.authdemo.exception.ResourceNotFoundException;
import com.ecommerce.authdemo.exception.OrderException;
import com.ecommerce.authdemo.repository.AddressRepository;
import com.ecommerce.authdemo.repository.OrderItemRepository;
import com.ecommerce.authdemo.repository.OrderRepository;
import com.ecommerce.authdemo.service.CartService;
import com.ecommerce.authdemo.service.OrderService;
import com.ecommerce.authdemo.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartService cartService;
    private final AddressRepository addressRepository;
    private final SecurityUtil securityUtil;

    @Override
    @Transactional
    public OrderResponseDTO placeOrder(PlaceOrderRequestDTO dto) {
        log.info("Placing order: paymentMethod={}, addressId={}", dto.getPaymentMethod(), dto.getAddressId());
        
        validatePlaceOrderRequest(dto);
        
        Long userId = securityUtil.getCurrentUserId();
        CartResponseDTO cart = cartService.getCart();

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new OrderException("Cart is empty. Cannot place order.");
        }

        Address address = addressRepository.findById(Math.toIntExact(dto.getAddressId()))
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        validateAddressOwnership(address);

        BigDecimal subtotal = cart.getPriceSummary().getSubtotal();
        BigDecimal shippingAmount = cart.getPriceSummary().getDeliveryCharge();
        BigDecimal discountAmount = cart.getPriceSummary().getDiscount();
        BigDecimal finalAmount = cart.getPriceSummary().getFinalTotal();

        Order order = createOrder(userId, dto, address, subtotal, shippingAmount, discountAmount, finalAmount);
        order = orderRepository.save(order);

        List<OrderItemDTO> itemDTOList = createOrderItems(order, cart.getItems());

        cartService.clearCart();
        log.info("Order placed successfully: orderId={}, orderNumber={}", order.getId(), order.getOrderNumber());

        return buildOrderResponse(order, itemDTOList);
    }

    @Override
    public List<OrderResponseDTO> getUserOrders(String status) {
        log.info("Fetching user orders with status: {}", status);
        
        Long userId = securityUtil.getCurrentUserId();
        List<Order> orders;

        if (status == null || status.equalsIgnoreCase("all")) {
            orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        } else {
            orders = orderRepository.findByUserIdAndOrderStatusOrderByCreatedAtDesc(userId, status.toLowerCase());
        }

        return orders.stream()
                .map(this::buildOrderSummaryResponse)
                .toList();
    }

    @Override
    public OrderResponseDTO getOrderDetails(Long orderId) {
        log.info("Fetching order details: orderId={}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        validateOrderOwnership(order);

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        List<OrderItemDTO> itemDTOList = items.stream()
                .map(this::buildOrderItemDTO)
                .toList();

        return buildDetailedOrderResponse(order, itemDTOList);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        log.info("Cancelling order: orderId={}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        validateOrderOwnership(order);

        if (!canCancelOrder(order)) {
            throw new OrderException("Order cannot be cancelled. Current status: " + order.getOrderStatus());
        }

        order.setOrderStatus("cancelled");
        orderRepository.save(order);
        
        log.info("Order cancelled successfully: orderId={}", orderId);
    }

    private void validatePlaceOrderRequest(PlaceOrderRequestDTO dto) {
        if (dto == null) {
            throw new OrderException("Place order request cannot be null");
        }
        if (dto.getAddressId() == null || dto.getAddressId() <= 0) {
            throw new OrderException("Valid address ID is required");
        }
        if (dto.getPaymentMethod() == null || dto.getPaymentMethod().trim().isEmpty()) {
            throw new OrderException("Payment method is required");
        }
    }

    private void validateAddressOwnership(Address address) {
        Long currentUserId = securityUtil.getCurrentUserId();
        if (!address.getUserId().equals(currentUserId)) {
            throw new OrderException("Access denied: You can only use your own addresses");
        }
    }

    private void validateOrderOwnership(Order order) {
        Long currentUserId = securityUtil.getCurrentUserId();
        if (!order.getUserId().equals(currentUserId)) {
            throw new OrderException("Access denied: You can only view your own orders");
        }
    }

    private boolean canCancelOrder(Order order) {
        String status = order.getOrderStatus();
        return !"delivered".equalsIgnoreCase(status) && !"cancelled".equalsIgnoreCase(status);
    }

    private Order createOrder(Long userId, PlaceOrderRequestDTO dto, Address address,
                             BigDecimal subtotal, BigDecimal shippingAmount, 
                             BigDecimal discountAmount, BigDecimal finalAmount) {
        
        String orderNumber = generateOrderNumber();
        
        return Order.builder()
                .userId(userId)
                .orderNumber(orderNumber)
                .totalAmount(subtotal.doubleValue())
                .shippingAmount(shippingAmount.doubleValue())
                .discountAmount(discountAmount.doubleValue())
                .taxAmount(0.0)
                .finalAmount(finalAmount.doubleValue())
                .paymentMethod(dto.getPaymentMethod())
                .paymentStatus("pending")
                .orderStatus("processing")
                .shippingName(address.getName())
                .shippingPhone(address.getPhone())
                .shippingEmail(address.getEmail())
                .shippingAddress1(address.getAddressLine1())
                .shippingCity(address.getCity())
                .shippingState(address.getState())
                .shippingPincode(address.getPincode())
                .addressId(Long.valueOf(address.getId()))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }

    private List<OrderItemDTO> createOrderItems(Order order, List<CartItemResponseDTO> cartItems) {
        List<OrderItemDTO> itemDTOList = new ArrayList<>();

        for (CartItemResponseDTO cartItem : cartItems) {
            OrderItem item = OrderItem.builder()
                    .orderId(order.getId())
                    .productId(cartItem.getProductId())
                    .variantId(cartItem.getVariantId())
                    .quantity(cartItem.getQuantity())
                    .price(cartItem.getPrice().doubleValue())
                    .total(cartItem.getTotal().doubleValue())
                    .status("processing")
                    .productImagePath(cartItem.getImageUrl())
                    .build();

            item = orderItemRepository.save(item);
            itemDTOList.add(buildOrderItemDTO(item));
        }

        return itemDTOList;
    }

    private OrderItemDTO buildOrderItemDTO(OrderItem item) {
        return OrderItemDTO.builder()
                .productId(item.getProductId())
                .productName("Product " + item.getProductId())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .total(item.getTotal())
                .productImage(item.getProductImagePath())
                .build();
    }

    private OrderResponseDTO buildOrderResponse(Order order, List<OrderItemDTO> itemDTOList) {
        return OrderResponseDTO.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .totalAmount(order.getTotalAmount())
                .finalAmount(order.getFinalAmount())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .items(itemDTOList)
                .shippingAmount(order.getShippingAmount())
                .discountAmount(order.getDiscountAmount())
                .paymentMethod(order.getPaymentMethod())
                .createdDate(formatDateTime(order.getCreatedAt()))
                .shippingAddress(buildShippingAddress(order))
                .build();
    }

    private OrderResponseDTO buildOrderSummaryResponse(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        
        return OrderResponseDTO.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .finalAmount(order.getFinalAmount())
                .totalItems(items.size())
                .firstProductImage(items.isEmpty() ? null : items.get(0).getProductImagePath())
                .createdDate(formatDateTime(order.getCreatedAt()))
                .build();
    }

    private OrderResponseDTO buildDetailedOrderResponse(Order order, List<OrderItemDTO> itemDTOList) {
        return OrderResponseDTO.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .totalAmount(order.getTotalAmount())
                .finalAmount(order.getFinalAmount())
                .shippingAmount(order.getShippingAmount())
                .discountAmount(order.getDiscountAmount())
                .paymentMethod(order.getPaymentMethod())
                .items(itemDTOList)
                .shippingAddress(buildShippingAddress(order))
                .createdDate(formatDateTime(order.getCreatedAt()))
                .build();
    }

    private String buildShippingAddress(Order order) {
        return String.format("%s, %s, %s - %s",
                order.getShippingAddress1(),
                order.getShippingCity(),
                order.getShippingState(),
                order.getShippingPincode());
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
