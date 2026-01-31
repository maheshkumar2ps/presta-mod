package com.prestashop.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateDto {

    @Size(max = 255, message = "Name must be less than 255 characters")
    private String name;

    private String description;

    private String descriptionShort;

    private String linkRewrite;

    @DecimalMin(value = "0.0", message = "Price must be positive")
    private BigDecimal price;

    private BigDecimal wholesalePrice;

    @Min(value = 0, message = "Quantity must be positive")
    private Integer quantity;

    @Min(value = 1, message = "Minimal quantity must be at least 1")
    private Integer minimalQuantity;

    @Size(max = 64, message = "Reference must be less than 64 characters")
    private String reference;

    @Size(max = 13, message = "EAN13 must be 13 characters")
    private String ean13;

    @Size(max = 32, message = "ISBN must be less than 32 characters")
    private String isbn;

    private BigDecimal weight;
    private BigDecimal width;
    private BigDecimal height;
    private BigDecimal depth;

    private Boolean active;
    private String visibility;
    private String condition;
    private String productType;
    private Boolean onSale;
    private Boolean onlineOnly;

    private String metaTitle;
    private String metaDescription;

    private Long defaultCategoryId;
    private List<Long> categoryIds;
}
