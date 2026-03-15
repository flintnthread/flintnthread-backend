package com.ecommerce.authdemo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

    @Data
    @Entity
    @Table(name = "product_pincodes")
    public class ProductPincode {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "product_id")
        private Long productId;

        @Column(name = "pincode_id")
        private Long pincodeId;

        private Integer status;

        @Column(name = "created_at")
        private LocalDateTime createdAt;
    }

