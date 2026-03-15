package com.ecommerce.authdemo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

    @Data
    @Entity
    @Table(name = "product_images")
    public class ProductImage {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name="product_id")
        private Long productId;

        @Column(name="variant_id")
        private Long variantId;

        @Column(name="image_path")
        private String imagePath;

        @Column(name="is_primary")
        private Boolean isPrimary;

        @Column(name="sort_order")
        private Integer sortOrder;

        @Column(name="created_at")
        private LocalDateTime createdAt;
    }

