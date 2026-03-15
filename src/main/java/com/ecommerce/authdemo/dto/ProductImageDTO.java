package com.ecommerce.authdemo.dto;

import lombok.Data;

    @Data
    public class ProductImageDTO {

        private Long id;

        private Long productId;

        private String imagePath;

        private Boolean isPrimary;

        private Integer sortOrder;
    }

