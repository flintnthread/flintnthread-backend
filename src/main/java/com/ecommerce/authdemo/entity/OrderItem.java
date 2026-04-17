package com.ecommerce.authdemo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter @Setter
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;
    private Long productId;
    private Long variantId;

    private Integer quantity;

    private Double price;
    private Double total;

    private String status;

    private String productImagePath;

}