package com.ecommerce.authdemo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "product_variants")
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // VARIANT ATTRIBUTES
    private String color;

    private String size;

    private String sku;

    // PRICING
    @Column(name = "base_price")
    private BigDecimal basePrice;

    @Column(name = "mrp_excl_gst")
    private BigDecimal mrpExclGst;

    private Integer stock;

    @Column(name = "mrp_price")
    private BigDecimal mrpPrice;

    @Column(name = "discount_percentage")
    private BigDecimal discountPercentage;

    @Column(name = "discount_amount")
    private BigDecimal discountAmount;

    @Column(name = "selling_price")
    private BigDecimal sellingPrice;

    @Column(name = "tax_percentage")
    private BigDecimal taxPercentage;

    @Column(name = "tax_amount")
    private BigDecimal taxAmount;

    @Column(name = "final_price")
    private BigDecimal finalPrice;

    @Column(name = "mrp_incl_gst")
    private BigDecimal mrpInclGst;

    // DELIVERY
    @Column(name = "intra_city_delivery_charge")
    private BigDecimal intraCityDeliveryCharge;

    @Column(name = "metro_metro_delivery_charge")
    private BigDecimal metroMetroDeliveryCharge;

    @Column(name = "total_price_intra_city")
    private BigDecimal totalPriceIntraCity;

    @Column(name = "total_price_metro_metro")
    private BigDecimal totalPriceMetroMetro;

    // COMMISSION
    @Column(name = "commission_percentage")
    private BigDecimal commissionPercentage;

    @Column(name = "commission_amount")
    private BigDecimal commissionAmount;

    // MEDIA
    @Column(name = "video_path")
    private String videoPath;

    // SHIPPING
    private BigDecimal weight;

    // TIMESTAMPS
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

}