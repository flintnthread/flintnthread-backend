package com.ecommerce.authdemo.dto;

import jakarta.persistence.Entity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductDTO {

    private Long id;
    private Long categoryId;
    private Long subcategoryId;
    private Long sellerId;

    private String name;
    private String sku;
    private String hsnCode;
    private String productMaterialType;

    private String shortDescription;
    private String description;
    private String features;
    private String specifications;
    private String returnPolicy;

    private BigDecimal gstPercentage;
    private BigDecimal productWeight;

    private Integer deliveryTimeMin;
    private Integer deliveryTimeMax;

    private String status;
    private LocalDateTime createdAt;

    // 🔥 RELATIONS
    private List<ProductImageDTO> images;
    private List<ProductVariantDTO> variants;


}