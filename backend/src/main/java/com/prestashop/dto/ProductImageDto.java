package com.prestashop.dto;

import com.prestashop.entity.ProductImage;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageDto {

    private Long id;
    private Long productId;
    private String url;
    private Integer position;
    private Boolean cover;
    private String legend;

    public static ProductImageDto fromEntity(ProductImage image) {
        return ProductImageDto.builder()
                .id(image.getId())
                .productId(image.getProduct().getId())
                .url(image.getUrl())
                .position(image.getPosition())
                .cover(image.getCover())
                .legend(image.getLegend())
                .build();
    }
}
