package com.ecommerce.authdemo.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
    @Builder
    public class OrderResponseDTO {

        private Long orderId;
        private String orderNumber;

        private Double totalAmount;
        private Double finalAmount;

        private String paymentStatus;
        private String orderStatus;

        private List<OrderItemDTO> items;
    }

