package com.ecommerce.authdemo.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cart")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Builder.Default
    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "final_amount", precision = 12, scale = 2)
    private BigDecimal finalAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "currency", length = 10)
    private String currency = "INR";

    @Builder.Default
    @JsonManagedReference
    @OneToMany(
            mappedBy = "cart",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<CartItem> items = new ArrayList<>();


    /**
     * Add item to cart
     */
    public void addItem(CartItem item) {
        items.add(item);
        item.setCart(this);
        recalculateCart();
    }

    /**
     * Remove item from cart
     */
    public void removeItem(CartItem item) {
        items.remove(item);
        item.setCart(null);
        recalculateCart();
    }

    /**
     * Recalculate cart totals
     */
    public void recalculateCart() {

        this.totalAmount = items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (discountAmount == null) {
            discountAmount = BigDecimal.ZERO;
        }

        this.finalAmount = totalAmount.subtract(discountAmount);
    }
}