package com.ecommerce.authdemo.entity;

import com.ecommerce.authdemo.dto.Enum.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String orderNumber;

    private Double totalAmount;
    private Double finalAmount;

    private Double shippingAmount;
    private Double taxAmount;
    private Double discountAmount;

    private String paymentMethod;
    private String paymentStatus;

    private String orderStatus;

    private String shippingName;
    private String shippingPhone;
    private String shippingAddress1;
    private String shippingCity;
    private String shippingState;
    private String shippingPincode;

    private Long addressId;

    private LocalDateTime createdAt;

}