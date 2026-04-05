package com.ecommerce.authdemo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "variant_id")
    private Long variantId;

    @Column(name = "seller_id")
    private Long sellerId;

    @Column(nullable = false)
    private Integer quantity;

    private BigDecimal unitPrice;


    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Builder.Default
    @Column(name = "discount", precision = 10, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "tax", precision = 10, scale = 2)
    private BigDecimal tax = BigDecimal.ZERO;

    @Column(name = "total", precision = 10, scale = 2)
    private BigDecimal total;

    @Builder.Default
    @Column(nullable = false)
    private String status = "active";

    @Column(name = "product_image_path", length = 500)
    private String productImagePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * Calculate total price
     */
    public void calculateTotal() {

        if (price == null || quantity == null) {
            return;
        }

        BigDecimal subtotal = price.multiply(BigDecimal.valueOf(quantity));

        if (discount != null) {
            subtotal = subtotal.subtract(discount);
        }

        if (tax != null) {
            subtotal = subtotal.add(tax);
        }

        this.total = subtotal;
    }
}