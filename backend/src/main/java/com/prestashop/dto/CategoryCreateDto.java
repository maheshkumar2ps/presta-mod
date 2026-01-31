package com.prestashop.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryCreateDto {

    @NotBlank(message = "Category name is required")
    @Size(max = 255, message = "Name must be less than 255 characters")
    private String name;

    private String description;

    private String linkRewrite;

    private Long parentId;

    private Integer position;

    private Boolean active = true;

    private String metaTitle;

    private String metaDescription;
}
