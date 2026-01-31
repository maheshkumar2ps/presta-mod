package com.prestashop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ps_specific_price")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecificPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_specific_price")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_product", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_product_attribute")
    private ProductAttribute productAttribute;

    @Enumerated(EnumType.STRING)
    @Column(name = "reduction_type", nullable = false)
    @Builder.Default
    private ReductionType reductionType = ReductionType.AMOUNT;

    @Column(precision = 20, scale = 6, nullable = false)
    private BigDecimal reduction;

    @Column(name = "reduction_tax")
    @Builder.Default
    private Boolean reductionTax = true;

    @Column(name = "from_quantity")
    @Builder.Default
    private Integer fromQuantity = 1;

    @Column(name = "from_date")
    private LocalDateTime fromDate;

    @Column(name = "to_date")
    private LocalDateTime toDate;

    public enum ReductionType {
        AMOUNT, PERCENTAGE
    }

    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        boolean afterStart = fromDate == null || now.isAfter(fromDate);
        boolean beforeEnd = toDate == null || now.isBefore(toDate);
        return afterStart && beforeEnd;
    }

    public BigDecimal calculateDiscountedPrice(BigDecimal originalPrice) {
        if (!isActive()) {
            return originalPrice;
        }

        if (reductionType == ReductionType.PERCENTAGE) {
            BigDecimal discount = originalPrice.multiply(reduction).divide(BigDecimal.valueOf(100));
            return originalPrice.subtract(discount);
        } else {
            return originalPrice.subtract(reduction);
        }
    }
}
