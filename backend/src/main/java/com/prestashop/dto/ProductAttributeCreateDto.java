package com.prestashop.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductAttributeCreateDto {

    @NotBlank(message = "Variant name is required")
    private String name;

    private String reference;
    private String ean13;
    private String isbn;
    private String upc;

    private BigDecimal priceImpact = BigDecimal.ZERO;
    private BigDecimal weightImpact = BigDecimal.ZERO;

    @Min(value = 0, message = "Quantity must be positive")
    private Integer quantity = 0;

    @Min(value = 1, message = "Minimal quantity must be at least 1")
    private Integer minimalQuantity = 1;

    private Boolean defaultOn = false;
}
