package com.ecommerce.authdemo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // CATEGORY
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "subcategory_id")
    private Long subcategoryId;

    // SELLER
    @Column(name = "seller_id")
    private Long sellerId;

    @Column(name = "size_chart_id")
    private Long sizeChartId;

    @Column(name = "weight_slab_id")
    private Long weightSlabId;

    // BASIC DETAILS
    @Column(name = "product_name")
    private String productName;

    private String sku;

    @Column(name = "hsn_code")
    private String hsnCode;

    @Column(name = "product_material_type")
    private String productMaterialType;

    @Column(name = "short_description", columnDefinition = "TEXT")
    private String shortDescription;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String features;

    @Column(columnDefinition = "TEXT")
    private String specifications;

    @Column(name = "return_policy", columnDefinition = "TEXT")
    private String returnPolicy;

    @Column(name = "delivery_info", columnDefinition = "TEXT")
    private String deliveryInfo;

    @Column(name = "warranty_info", columnDefinition = "TEXT")
    private String warrantyInfo;

    @Column(name = "care_instructions", columnDefinition = "TEXT")
    private String careInstructions;

    // GST
    @Column(name = "gst_percentage")
    private BigDecimal gstPercentage;

    // DIMENSIONS
    @Column(name = "length_cm")
    private BigDecimal lengthCm;

    @Column(name = "width_cm")
    private BigDecimal widthCm;

    @Column(name = "height_cm")
    private BigDecimal heightCm;

    @Column(name = "product_weight")
    private BigDecimal productWeight;

    // DELIVERY
    @Column(name = "delivery_time_min")
    private Integer deliveryTimeMin;

    @Column(name = "delivery_time_max")
    private Integer deliveryTimeMax;

    @Column(name = "intra_city_charge")
    private BigDecimal intraCityCharge;

    @Column(name = "metro_metro_charge")
    private BigDecimal metroMetroCharge;

    // FLAGS
    @Column(name = "is_fragile")
    private Boolean isFragile;

    @Column(name = "is_custom_pricing")
    private Boolean isCustomPricing;

    @Column(name = "deliver_all_locations")
    private Boolean deliverAllLocations;

    // STATUS
    private String status;

    // TIMESTAMPS
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}