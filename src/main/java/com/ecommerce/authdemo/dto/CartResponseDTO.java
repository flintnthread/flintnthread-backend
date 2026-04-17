package com.ecommerce.authdemo.dto;

import lombok.Data;
import java.util.List;

    @Data
    public class CartResponseDTO {

        private List<CartItemResponseDTO> items;
        private PriceSummaryDTO priceSummary;
        private String couponApplied;
    }

