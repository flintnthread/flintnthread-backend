package com.ecommerce.authdemo.dto;

import lombok.Data;
import java.math.BigDecimal;

    @Data
    public class CartItemResponseDTO {

        private Long itemId;
        private Long productId;
        private String name;
        private String image;

        private BigDecimal price;
        private BigDecimal originalPrice;

        private Integer quantity;
        private BigDecimal total;

        private Long variantId;
    }

