package com.prestashop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ps_product_attribute")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_product_attribute")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_product", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String name;

    @Column(length = 64)
    private String reference;

    @Column(length = 13)
    private String ean13;

    @Column(length = 32)
    private String isbn;

    @Column(length = 12)
    private String upc;

    @Column(length = 40)
    private String mpn;

    @Column(precision = 20, scale = 6)
    @Builder.Default
    private BigDecimal priceImpact = BigDecimal.ZERO;

    @Column(precision = 20, scale = 6)
    @Builder.Default
    private BigDecimal weightImpact = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    @Column(name = "minimal_quantity")
    @Builder.Default
    private Integer minimalQuantity = 1;

    @Column(name = "low_stock_threshold")
    private Integer lowStockThreshold;

    @Column(name = "default_on")
    @Builder.Default
    private Boolean defaultOn = false;

    public BigDecimal getFinalPrice() {
        return product.getPrice().add(priceImpact);
    }

    public BigDecimal getFinalWeight() {
        BigDecimal baseWeight = product.getWeight() != null ? product.getWeight() : BigDecimal.ZERO;
        return baseWeight.add(weightImpact != null ? weightImpact : BigDecimal.ZERO);
    }

    public boolean isInStock() {
        return quantity > 0;
    }
}
