package com.ecommerce.authdemo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_reviews")
@Getter
@Setter
    public class ProductReview {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    private Long userId;

        private String name;
        private String email;

        private Integer rating;
        private String comment;

        private Boolean status = true;

        @CreationTimestamp
        private LocalDateTime createdAt;
    }

