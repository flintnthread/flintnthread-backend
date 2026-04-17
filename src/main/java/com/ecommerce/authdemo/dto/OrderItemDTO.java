package com.ecommerce.authdemo.dto;


import lombok.Builder;
import lombok.Data;

@Data
    @Builder
    public class OrderItemDTO {

        private Long productId;
        private String productImage;

        private Integer quantity;
        private Double price;
        private Double total;
    }

