package com.ecommerce.authdemo.entity;

import com.ecommerce.authdemo.dto.Enum.DeliveryType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cart_item")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    private Long sellerId;

    private BigDecimal unitPrice;

    @Column(name = "variant_id")
    private Long variantId;

    private Integer quantity;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "total_price", precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_type")
    private DeliveryType deliveryType;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    /**
     * Calculate total price
     */
    public void calculateTotalPrice() {
        if (price != null && quantity != null) {
            this.totalPrice = price.multiply(BigDecimal.valueOf(quantity));
        }
    }
}