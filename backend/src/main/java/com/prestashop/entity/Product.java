package com.prestashop.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "ps_product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_product")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "description_short", columnDefinition = "TEXT")
    private String descriptionShort;

    @Column(name = "link_rewrite", nullable = false, unique = true)
    private String linkRewrite;

    @Column(precision = 20, scale = 6, nullable = false)
    private BigDecimal price;

    @Column(name = "wholesale_price", precision = 20, scale = 6)
    private BigDecimal wholesalePrice;

    @Column(precision = 20, scale = 6)
    private BigDecimal ecotax;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    @Column(name = "minimal_quantity")
    @Builder.Default
    private Integer minimalQuantity = 1;

    @Column(name = "low_stock_threshold")
    private Integer lowStockThreshold;

    @Column(length = 64)
    private String reference;

    @Column(length = 13)
    private String ean13;

    @Column(length = 32)
    private String isbn;

    @Column(length = 12)
    private String upc;

    @Column(precision = 20, scale = 6)
    private BigDecimal weight;

    @Column(precision = 20, scale = 6)
    private BigDecimal width;

    @Column(precision = 20, scale = 6)
    private BigDecimal height;

    @Column(precision = 20, scale = 6)
    private BigDecimal depth;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Visibility visibility = Visibility.BOTH;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_condition", nullable = false)
    @Builder.Default
    private ProductCondition condition = ProductCondition.NEW;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false)
    @Builder.Default
    private ProductType productType = ProductType.STANDARD;

    @Column(name = "on_sale")
    @Builder.Default
    private Boolean onSale = false;

    @Column(name = "online_only")
    @Builder.Default
    private Boolean onlineOnly = false;

    @Column(name = "available_for_order")
    @Builder.Default
    private Boolean availableForOrder = true;

    @Column(name = "show_price")
    @Builder.Default
    private Boolean showPrice = true;

    @Column(name = "meta_title")
    private String metaTitle;

    @Column(name = "meta_description", columnDefinition = "TEXT")
    private String metaDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_category_default")
    private Category defaultCategory;

    @ManyToMany
    @JoinTable(
        name = "ps_category_product",
        joinColumns = @JoinColumn(name = "id_product"),
        inverseJoinColumns = @JoinColumn(name = "id_category")
    )
    @Builder.Default
    private Set<Category> categories = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductAttribute> attributes = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SpecificPrice> specificPrices = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "date_add", updatable = false)
    private LocalDateTime dateAdd;

    @UpdateTimestamp
    @Column(name = "date_upd")
    private LocalDateTime dateUpd;

    public enum Visibility {
        BOTH, CATALOG, SEARCH, NONE
    }

    public enum ProductCondition {
        NEW, USED, REFURBISHED
    }

    public enum ProductType {
        STANDARD, PACK, VIRTUAL, COMBINATIONS
    }

    // Helper methods
    public void addImage(ProductImage image) {
        images.add(image);
        image.setProduct(this);
    }

    public void removeImage(ProductImage image) {
        images.remove(image);
        image.setProduct(null);
    }

    public void addCategory(Category category) {
        categories.add(category);
    }

    public void removeCategory(Category category) {
        categories.remove(category);
    }

    public ProductImage getCoverImage() {
        return images.stream()
                .filter(ProductImage::getCover)
                .findFirst()
                .orElse(images.isEmpty() ? null : images.get(0));
    }
}
