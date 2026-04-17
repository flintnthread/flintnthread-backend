package com.ecommerce.authdemo.dto;


import lombok.Data;

    @Data
    public class AddToCartDTO {

        private Long productId;
        private Long variantId;
        private Integer quantity;
    }

