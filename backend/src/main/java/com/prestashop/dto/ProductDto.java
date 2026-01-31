package com.prestashop.dto;

import com.prestashop.entity.Product;
import com.prestashop.entity.ProductImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {
    private static Logger LOGGER = LoggerFactory.getLogger(ProductDto.class);

    private Long id;
    private String name;
    private String description;
    private String descriptionShort;
    private String linkRewrite;
    private BigDecimal price;
    private BigDecimal salePrice;
    private BigDecimal wholesalePrice;
    private String reference;
    private String ean13;
    private Integer quantity;
    private Boolean inStock;
    private Boolean active;
    private String visibility;
    private String condition;
    private String productType;
    private Boolean onSale;
    private BigDecimal weight;
    private String metaTitle;
    private String metaDescription;
    private CategoryDto defaultCategory;
    private List<CategoryDto> categories;
    private List<ProductImageDto> images;
    private String coverImage;
    private List<ProductAttributeDto> variants;
    private LocalDateTime dateAdd;
    private LocalDateTime dateUpd;

    public static ProductDto fromEntity(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .descriptionShort(product.getDescriptionShort())
                .linkRewrite(product.getLinkRewrite())
                .price(product.getPrice())
                .wholesalePrice(product.getWholesalePrice())
                .reference(product.getReference())
                .ean13(product.getEan13())
                .quantity(product.getQuantity())
                .inStock(product.getQuantity() > 0)
                .active(product.getActive())
                .visibility(product.getVisibility().name())
                .condition(product.getCondition().name())
                .productType(product.getProductType().name())
                .onSale(product.getOnSale())
                .weight(product.getWeight())
                .metaTitle(product.getMetaTitle())
                .metaDescription(product.getMetaDescription())
                .dateAdd(product.getDateAdd())
                .dateUpd(product.getDateUpd())
                .build();
    }

    public static ProductDto forListing(Product product) {
        ProductImage coverImage = product.getCoverImage();
        String coverUrl = null;

        if (coverImage != null) {
            coverUrl = coverImage.getUrl();
            LOGGER.debug("Product {} ({}) - forListing - Cover image: id={}, s3Key={}, s3Url={}, filename={}, resolvedUrl={}",
                    product.getId(), product.getName(), coverImage.getId(),
                    coverImage.getS3Key(), coverImage.getS3Url(),
                    coverImage.getFilename(), coverUrl);
        } else {
            LOGGER.debug("Product {} ({}) - forListing - No cover image", product.getId(), product.getName());
        }

        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .descriptionShort(product.getDescriptionShort())
                .linkRewrite(product.getLinkRewrite())
                .price(product.getPrice())
                .reference(product.getReference())
                .quantity(product.getQuantity())
                .inStock(product.getQuantity() > 0)
                .onSale(product.getOnSale())
                .coverImage(coverUrl)
                .defaultCategory(product.getDefaultCategory() != null
                    ? CategoryDto.simple(product.getDefaultCategory()) : null)
                .build();
    }
}
