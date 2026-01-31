package com.prestashop.dto;

import com.prestashop.entity.ProductAttribute;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductAttributeDto {

    private Long id;
    private Long productId;
    private String name;
    private String reference;
    private String ean13;
    private BigDecimal price;
    private BigDecimal priceImpact;
    private Integer quantity;
    private Boolean inStock;
    private Boolean defaultOn;

    public static ProductAttributeDto fromEntity(ProductAttribute attr) {
        return ProductAttributeDto.builder()
                .id(attr.getId())
                .productId(attr.getProduct().getId())
                .name(attr.getName())
                .reference(attr.getReference())
                .ean13(attr.getEan13())
                .price(attr.getFinalPrice())
                .priceImpact(attr.getPriceImpact())
                .quantity(attr.getQuantity())
                .inStock(attr.isInStock())
                .defaultOn(attr.getDefaultOn())
                .build();
    }
}
