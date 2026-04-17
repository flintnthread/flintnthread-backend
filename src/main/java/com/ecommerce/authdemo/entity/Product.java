package com.ecommerce.authdemo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "products")
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String sku;

    private String shortDescription;
    private String description;

    private String features;
    private String specifications;
    private String returnPolicy;

    private BigDecimal gstPercentage;

    private BigDecimal lengthCm;
    private BigDecimal widthCm;
    private BigDecimal heightCm;

    private BigDecimal productWeight;

    private Boolean isFragile = false;

    private Integer categoryId;
    private Integer subcategoryId;

    private Long sellerId;

    private Boolean acceptCod = true;
    private Boolean acceptPrepaid = true;

    private Boolean deliverAllLocations = true;

    private String status;

    private Integer deliveryTimeMin;
    private Integer deliveryTimeMax;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ------------------------
    // RELATIONS
    // ------------------------

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<ProductVariant> variants;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private Set<ProductImage> images;


    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<ProductView> views;
}