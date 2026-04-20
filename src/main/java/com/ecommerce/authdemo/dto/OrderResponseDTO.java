package com.ecommerce.authdemo.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrderResponseDTO {

    private Long orderId;
    private String orderNumber;

    private String orderStatus;
    private String paymentStatus;
    private String paymentMethod;

    private Double totalAmount;
    private Double finalAmount;
    private Double shippingAmount;
    private Double discountAmount;

    private Integer totalItems;
    private String firstProductImage;
    private String trackingNumber;
    private String createdDate;

    private String shippingAddress;

    private List<OrderItemDTO> items;
}