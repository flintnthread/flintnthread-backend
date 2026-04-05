package com.ecommerce.authdemo.entity;

import com.ecommerce.authdemo.dto.Enum.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", unique = true, nullable = false)
    private String orderNumber;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "address_id")
    private Long addressId;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    private OrderStatus status;

    @Builder.Default
    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "shipping_amount", precision = 10, scale = 2)
    private BigDecimal shippingCharge = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "final_amount", precision = 10, scale = 2)
    private BigDecimal finalAmount = BigDecimal.ZERO;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Builder.Default
    @Column(name = "payment_completed")
    private Boolean paymentCompleted = false;

    @Builder.Default
    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItem> items = new ArrayList<>();


    /**
     * Add item to order
     */
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    /**
     * Remove item from order
     */
    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }

    /**
     * Calculate totals
     */
    public void calculateAmounts() {

        this.totalAmount = items.stream()
                .map(OrderItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.finalAmount = totalAmount
                .add(taxAmount)
                .add(shippingCharge)
                .subtract(discountAmount);
    }
}