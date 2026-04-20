package com.ecommerce.authdemo.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartItemResponseDTO {

    private Long itemId;
    private Long productId;

    private String name;
    private String imageUrl;

    private String productName;

    private BigDecimal price;
    private BigDecimal originalPrice;

    /** Unit selling price (same semantics as {@code price}; explicit for clients). */
    private BigDecimal sellingPrice;

    /** Unit MRP / list price before line discount. */
    private BigDecimal mrpPrice;

    private Integer quantity;

    private BigDecimal total;

    private Long variantId;

    private String size;
    private String color;

    /** Display color name (same as {@code color}; explicit for clients). */
    private String colorName;

}