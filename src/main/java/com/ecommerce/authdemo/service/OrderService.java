package com.ecommerce.authdemo.service;

import com.ecommerce.authdemo.dto.OrderResponseDTO;
import com.ecommerce.authdemo.dto.PlaceOrderRequestDTO;
import com.ecommerce.authdemo.entity.Order;

import java.util.List;

public interface OrderService {

    OrderResponseDTO placeOrder(PlaceOrderRequestDTO dto);

    List<OrderResponseDTO> getUserOrders(String status);

    OrderResponseDTO getOrderDetails(Long orderId);

    void cancelOrder(Long orderId);

    Order markOrderAsPaid(String razorpayOrderId, String paymentId);

    void updateShipment(String orderNumber, String awb, String courier, String trackingUrl, String shiprocketStatus);

    void markShiprocketCreateFailed(String orderNumber, String reason);

    void updateOrderStatusFromWebhook(String awb, String status);

    void linkRazorpayOrder(String razorpayOrderId);
}