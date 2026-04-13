package com.ecommerce.authdemo.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data

public class WishlistResponse {

    private Long wishlistId;
    private Long productId;
    private String productName;
    private String image;
    private String size;
    private String color;
    private Boolean inStock;
    private BigDecimal sellingPrice;
    private BigDecimal mrpPrice;


    private LocalDateTime addedAt;

    // getters and setters
}