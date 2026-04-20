package com.ecommerce.authdemo.service;

import com.ecommerce.authdemo.dto.OrderResponseDTO;
import com.ecommerce.authdemo.dto.PlaceOrderRequestDTO;

import java.util.List;

public interface OrderService {

    OrderResponseDTO placeOrder(PlaceOrderRequestDTO dto);

    List<OrderResponseDTO> getUserOrders(String status);

    OrderResponseDTO getOrderDetails(Long orderId);

    void cancelOrder(Long orderId);
}