package com.ecommerce.authdemo.service.impl;

import com.ecommerce.authdemo.dto.*;
import com.ecommerce.authdemo.entity.*;
import com.ecommerce.authdemo.exception.ResourceNotFoundException;
import com.ecommerce.authdemo.exception.OrderException;
import com.ecommerce.authdemo.repository.AddressRepository;
import com.ecommerce.authdemo.repository.CartRepository;
import com.ecommerce.authdemo.repository.OrderItemRepository;
import com.ecommerce.authdemo.repository.OrderRepository;
import com.ecommerce.authdemo.repository.ProductRepository;
import com.ecommerce.authdemo.service.CartService;
import com.ecommerce.authdemo.service.OrderService;
import com.ecommerce.authdemo.service.ReferralService;
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
import java.util.Optional;

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
    private final SecurityUtil securityUtil;
    private final ReferralService referralService;

    @Override
    @Transactional
    public OrderResponseDTO placeOrder(PlaceOrderRequestDTO dto) {
        log.info("Placing order: paymentMethod={}, addressId={}", dto.getPaymentMethod(), dto.getAddressId());

        try {
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
            boolean referralDiscountApplied = false;
            // Apply referral reward on the next order (one-time), regardless of prior orders.
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
                discountAmount = discountAmount.add(extraDiscount);
                finalAmount = subtotal.add(shippingAmount).subtract(discountAmount).max(BigDecimal.ZERO);
                referralDiscountApplied = true;
            }

            Order order = createOrder(userId, dto, address, subtotal, shippingAmount, discountAmount, finalAmount);
            order = orderRepository.saveAndFlush(order);

            if (referralDiscountApplied) {
                try {
                    referralService.markReferralDiscountUsed(userId, order.getId());
                } catch (Exception e) {
                    log.warn("[ORDER] referral discount mark-used failed userId={} orderId={}: {}", userId, order.getId(), e.getMessage());
                }
            }

            List<OrderItemDTO> itemDTOList = createOrderItems(order, cart.getItems());
            orderItemRepository.flush();

            clearCartSafely(userId);

            log.info("[ORDER] placeOrder DONE orderId={} orderNumber={} paymentStatus=pending (pay next)",
                    order.getId(), order.getOrderNumber());
            return buildOrderResponse(order, itemDTOList);
        } catch (Exception e) {
            log.error("[ORDER] placeOrder FAILED: {}", e.getMessage(), e);
            throw e;
        }
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
                .totalAmount(finalAmount.doubleValue())
                .shippingAmount(shippingAmount.doubleValue())
                .discountAmount(discountAmount.doubleValue())
                .taxAmount(0.0)
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

    private void clearCartSafely(Long userId) {
        try {
            cartRepository.deleteByUser_Id(userId);
        } catch (Exception e) {
            log.warn("[ORDER] cart clear failed after order persist userId={}: {}", userId, e.getMessage());
        }
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }

    private List<OrderItemDTO> createOrderItems(Order order, List<CartItemResponseDTO> cartItems) {
        List<OrderItemDTO> itemDTOList = new ArrayList<>();

        for (CartItemResponseDTO cartItem : cartItems) {
            Long sellerId = productRepository.findById(cartItem.getProductId())
                    .map(Product::getSellerId)
                    .orElse(null);

            OrderItem item = OrderItem.builder()
                    .orderId(order.getId())
                    .productId(cartItem.getProductId())
                    .variantId(cartItem.getVariantId())
                    .quantity(cartItem.getQuantity())
                    .price(cartItem.getPrice().doubleValue())
                    .total(cartItem.getTotal().doubleValue())
                    .status("processing")
                    .productImagePath(cartItem.getImageUrl())
                    .sellerId(sellerId)
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
                .finalAmount(order.getTotalAmount())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .items(itemDTOList)
                .shippingAmount(order.getShippingAmount())
                .discountAmount(order.getDiscountAmount())
                .paymentMethod(order.getPaymentMethod())
                .createdDate(formatDateTime(order.getCreatedAt()))
                .shippingAddress(buildShippingAddress(order))
                .shiprocketAwbCode(order.getShiprocketAwbCode())
                .shiprocketTrackingUrl(order.getShiprocketTrackingUrl())
                .shiprocketCourierName(order.getShiprocketCourierName())
                .shiprocketStatus(order.getShiprocketStatus())
                .build();
    }

    private OrderResponseDTO buildOrderSummaryResponse(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        List<OrderItemDTO> itemDTOList = items.stream().map(this::buildOrderItemDTO).toList();

        return OrderResponseDTO.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .totalAmount(order.getTotalAmount())
                .finalAmount(order.getTotalAmount())
                .totalItems(items.size())
                .firstProductImage(items.isEmpty() ? null : items.get(0).getProductImagePath())
                .items(itemDTOList)
                .createdDate(formatDateTime(order.getCreatedAt()))
                .shiprocketAwbCode(order.getShiprocketAwbCode())
                .shiprocketTrackingUrl(order.getShiprocketTrackingUrl())
                .shiprocketCourierName(order.getShiprocketCourierName())
                .shiprocketStatus(order.getShiprocketStatus())
                .build();
    }

    private OrderResponseDTO buildDetailedOrderResponse(Order order, List<OrderItemDTO> itemDTOList) {
        return OrderResponseDTO.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .totalAmount(order.getTotalAmount())
                .finalAmount(order.getTotalAmount())
                .shippingAmount(order.getShippingAmount())
                .discountAmount(order.getDiscountAmount())
                .paymentMethod(order.getPaymentMethod())
                .items(itemDTOList)
                .shippingAddress(buildShippingAddress(order))
                .createdDate(formatDateTime(order.getCreatedAt()))
                .shiprocketAwbCode(order.getShiprocketAwbCode())
                .shiprocketTrackingUrl(order.getShiprocketTrackingUrl())
                .shiprocketCourierName(order.getShiprocketCourierName())
                .shiprocketStatus(order.getShiprocketStatus())
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

    @Override
    @Transactional
    public Order markOrderAsPaid(String razorpayOrderId, String paymentId) {

        List<Order> matches = orderRepository.findByRazorpayOrderIdOrderByCreatedAtDesc(razorpayOrderId);
        if (matches.isEmpty()) {
            throw new RuntimeException("Order not found");
        }
        if (matches.size() > 1) {
            log.warn("[ORDER] markOrderAsPaid duplicate razorpayOrderId={} count={} -> choosing latest orderId={}",
                    razorpayOrderId, matches.size(), matches.get(0).getId());
        }
        Order order = matches.get(0);

        order.setPaymentStatus("paid");
        order.setOrderStatus("processing");
        order.setRazorpayPaymentId(paymentId);

        Order saved = orderRepository.save(order);
        if (orderRepository.countByUserIdAndPaymentStatus(saved.getUserId(), "paid") == 1) {
            referralService.confirmReferralOnFirstPaidOrder(saved.getUserId());
        }
        log.info("[ORDER] markOrderAsPaid orderNumber={} orderId={} paymentStatus=paid razorpayPaymentId={}",
                saved.getOrderNumber(), saved.getId(), paymentId);
        return saved;
    }

    @Override
    @Transactional
    public void updateShipment(String orderNumber, String awb, String courier, String trackingUrl, String shiprocketStatus) {

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setShiprocketAwbCode(awb);
        order.setShiprocketCourierName(courier);
        order.setShiprocketTrackingUrl(trackingUrl);
        order.setShiprocketStatus(shiprocketStatus != null ? shiprocketStatus : "label_created");
        order.setOrderStatus("processing");

        orderRepository.save(order);
        log.info("[ORDER] updateShipment orderNumber={} awb={} shiprocketStatus={} trackingUrl={}",
                orderNumber, awb, order.getShiprocketStatus(), trackingUrl);
    }

    @Override
    @Transactional
    public void markShiprocketCreateFailed(String orderNumber, String reason) {
        orderRepository.findByOrderNumber(orderNumber).ifPresentOrElse(order -> {
            order.setShiprocketStatus("create_failed");
            if (order.getShiprocketAwbCode() != null && order.getShiprocketAwbCode().startsWith("TEMP_AWB_")) {
                order.setShiprocketAwbCode(null);
                order.setShiprocketTrackingUrl(null);
            }
            orderRepository.save(order);
            log.warn("[ORDER] markShiprocketCreateFailed orderNumber={} reason={}", orderNumber, reason);
        }, () -> log.warn("[ORDER] markShiprocketCreateFailed no order for orderNumber={}", orderNumber));
    }

    @Override
    @Transactional
    public void updateOrderStatusFromWebhook(String awb, String status) {
        log.info("[ORDER:WEBHOOK] update status awb={} shiprocketStatus={}", awb, status);

        try {
            Order order = orderRepository.findByShiprocketAwbCode(awb)
                    .orElseThrow(() -> new RuntimeException("Order not found for AWB: " + awb));

            String mappedStatus = mapShiprocketStatus(status);
            order.setOrderStatus(mappedStatus);
            if (status != null && !status.isBlank()) {
                order.setShiprocketStatus(status);
            }

            orderRepository.save(order);

            log.info("[ORDER:WEBHOOK] updated orderNumber={} orderStatus={} shiprocketStatus={}",
                    order.getOrderNumber(), mappedStatus, order.getShiprocketStatus());
        } catch (Exception e) {
            log.warn("[ORDER:WEBHOOK] failed awb={}: {}", awb, e.getMessage());
        }
    }

    @Override
    @Transactional
    public void linkRazorpayOrder(String razorpayOrderId) {
        log.info("[ORDER] linkRazorpayOrder start razorpayOrderId={}", razorpayOrderId);

        Optional<Long> maybeUserId = securityUtil.tryGetCurrentUserId();
        Order order = maybeUserId
                .flatMap(uid -> orderRepository.findTopByUserIdAndPaymentStatusOrderByCreatedAtDesc(uid, "pending"))
                .or(() -> orderRepository.findTopByPaymentStatusOrderByCreatedAtDesc("pending"))
                .orElseThrow(() -> new RuntimeException("No pending order found"));

        order.setRazorpayOrderId(razorpayOrderId);
        orderRepository.save(order);

        log.info("[ORDER] linkRazorpayOrder done orderNumber={} orderId={} userScope={} paymentStatus=pending (awaiting verify)",
                order.getOrderNumber(), order.getId(), maybeUserId.isPresent());
    }

    private String mapShiprocketStatus(String shiprocketStatus) {
        if (shiprocketStatus == null) return "processing";
        
        switch (shiprocketStatus.toUpperCase()) {
            case "PICKED_UP":
            case "IN_TRANSIT":
                return "processing";
            case "OUT_FOR_DELIVERY":
                return "processing";
            case "DELIVERED":
                return "completed";
            case "CANCELLED":
            case "RTO":
                return "cancelled";
            default:
                return "processing";
        }
    }

}
