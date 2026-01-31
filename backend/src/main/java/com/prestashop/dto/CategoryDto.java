package com.prestashop.dto;

import com.prestashop.entity.Category;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDto {

    private Long id;
    private String name;
    private String description;
    private String linkRewrite;
    private Long parentId;
    private Integer levelDepth;
    private Integer position;
    private Boolean active;
    private String metaTitle;
    private String metaDescription;
    private List<CategoryDto> children;
    private List<BreadcrumbDto> breadcrumb;

    public static CategoryDto fromEntity(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .linkRewrite(category.getLinkRewrite())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .levelDepth(category.getLevelDepth())
                .position(category.getPosition())
                .active(category.getActive())
                .metaTitle(category.getMetaTitle())
                .metaDescription(category.getMetaDescription())
                .build();
    }

    public static CategoryDto withChildren(Category category) {
        CategoryDto dto = fromEntity(category);
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            dto.setChildren(category.getChildren().stream()
                    .filter(Category::getActive)
                    .map(CategoryDto::withChildren)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    public static CategoryDto withBreadcrumb(Category category) {
        CategoryDto dto = fromEntity(category);
        dto.setBreadcrumb(category.getBreadcrumb().stream()
                .map(c -> new BreadcrumbDto(c.getId(), c.getName(), c.getLinkRewrite()))
                .collect(Collectors.toList()));
        return dto;
    }

    public static CategoryDto simple(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .linkRewrite(category.getLinkRewrite())
                .build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BreadcrumbDto {
        private Long id;
        private String name;
        private String linkRewrite;
    }
}
