package com.prestashop.service;

import com.prestashop.dto.*;
import com.prestashop.entity.*;
import com.prestashop.exception.ResourceNotFoundException;
import com.prestashop.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository imageRepository;
    private final ProductAttributeRepository attributeRepository;
    private final SpecificPriceRepository specificPriceRepository;

    // Public methods for PLP/PDP

    @Transactional(readOnly = true)
    public Page<ProductDto> getProducts(Pageable pageable) {
        return productRepository.findAllActive(pageable)
                .map(ProductDto::forListing);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable)
                .map(ProductDto::forListing);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> getProductsByCategorySlug(String slug, Pageable pageable) {
        Category category = categoryRepository.findByLinkRewrite(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + slug));
        return getProductsByCategory(category.getId(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> searchProducts(String query, Pageable pageable) {
        return productRepository.search(query, pageable)
                .map(ProductDto::forListing);
    }

    @Transactional(readOnly = true)
    public ProductDto getProductBySlug(String slug) {
        Product product = productRepository.findByLinkRewrite(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + slug));
        return buildFullProductDto(product);
    }

    @Transactional(readOnly = true)
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        return buildFullProductDto(product);
    }

    private ProductDto buildFullProductDto(Product product) {
        ProductDto dto = ProductDto.fromEntity(product);

        // Add images
        dto.setImages(product.getImages().stream()
                .map(ProductImageDto::fromEntity)
                .collect(Collectors.toList()));

        // Set cover image
        ProductImage cover = product.getCoverImage();
        if (cover != null) {
            dto.setCoverImage(cover.getUrl());
        }

        // Add variants
        dto.setVariants(product.getAttributes().stream()
                .map(ProductAttributeDto::fromEntity)
                .collect(Collectors.toList()));

        // Add categories
        if (product.getDefaultCategory() != null) {
            dto.setDefaultCategory(CategoryDto.withBreadcrumb(product.getDefaultCategory()));
        }
        dto.setCategories(product.getCategories().stream()
                .map(CategoryDto::simple)
                .collect(Collectors.toList()));

        // Calculate sale price
        dto.setSalePrice(calculateSalePrice(product));

        return dto;
    }

    private java.math.BigDecimal calculateSalePrice(Product product) {
        List<SpecificPrice> activeSpecificPrices = specificPriceRepository.findActiveByProductId(
                product.getId(), LocalDateTime.now(), 1);

        if (!activeSpecificPrices.isEmpty()) {
            SpecificPrice sp = activeSpecificPrices.get(0);
            return sp.calculateDiscountedPrice(product.getPrice());
        }
        return null;
    }

    // Admin methods

    @Transactional(readOnly = true)
    public Page<ProductDto> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(ProductDto::fromEntity);
    }

    @Transactional
    public ProductDto createProduct(ProductCreateDto createDto) {
        Product product = new Product();
        mapCreateDtoToEntity(createDto, product);

        // Generate link_rewrite if not provided
        if (product.getLinkRewrite() == null || product.getLinkRewrite().isBlank()) {
            product.setLinkRewrite(generateLinkRewrite(product.getName()));
        }

        // Ensure unique link_rewrite
        String baseSlug = product.getLinkRewrite();
        int counter = 1;
        while (productRepository.existsByLinkRewrite(product.getLinkRewrite())) {
            product.setLinkRewrite(baseSlug + "-" + counter++);
        }

        product = productRepository.save(product);
        return ProductDto.fromEntity(product);
    }

    @Transactional
    public ProductDto updateProduct(Long id, ProductUpdateDto updateDto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

        mapUpdateDtoToEntity(updateDto, product);

        product = productRepository.save(product);
        return buildFullProductDto(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found: " + id);
        }
        productRepository.deleteById(id);
    }

    @Transactional
    public void bulkUpdateStatus(List<Long> ids, boolean active) {
        ids.forEach(id -> {
            productRepository.findById(id).ifPresent(product -> {
                product.setActive(active);
                productRepository.save(product);
            });
        });
    }

    @Transactional
    public void bulkDelete(List<Long> ids) {
        productRepository.deleteAllById(ids);
    }

    // Variant management

    @Transactional(readOnly = true)
    public List<ProductAttributeDto> getProductVariants(Long productId) {
        return attributeRepository.findByProductId(productId).stream()
                .map(ProductAttributeDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductAttributeDto addVariant(Long productId, ProductAttributeCreateDto createDto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        ProductAttribute attr = new ProductAttribute();
        attr.setProduct(product);
        attr.setName(createDto.getName());
        attr.setReference(createDto.getReference());
        attr.setEan13(createDto.getEan13());
        attr.setIsbn(createDto.getIsbn());
        attr.setUpc(createDto.getUpc());
        attr.setPriceImpact(createDto.getPriceImpact());
        attr.setWeightImpact(createDto.getWeightImpact());
        attr.setQuantity(createDto.getQuantity());
        attr.setMinimalQuantity(createDto.getMinimalQuantity());
        attr.setDefaultOn(createDto.getDefaultOn());

        attr = attributeRepository.save(attr);
        return ProductAttributeDto.fromEntity(attr);
    }

    @Transactional
    public void deleteVariant(Long productId, Long variantId) {
        ProductAttribute attr = attributeRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found: " + variantId));

        if (!attr.getProduct().getId().equals(productId)) {
            throw new IllegalArgumentException("Variant does not belong to product");
        }

        attributeRepository.delete(attr);
    }

    // Helper methods

    private void mapCreateDtoToEntity(ProductCreateDto dto, Product product) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setDescriptionShort(dto.getDescriptionShort());
        product.setLinkRewrite(dto.getLinkRewrite());
        product.setPrice(dto.getPrice());
        product.setWholesalePrice(dto.getWholesalePrice());
        product.setQuantity(dto.getQuantity() != null ? dto.getQuantity() : 0);
        product.setMinimalQuantity(dto.getMinimalQuantity() != null ? dto.getMinimalQuantity() : 1);
        product.setReference(dto.getReference());
        product.setEan13(dto.getEan13());
        product.setIsbn(dto.getIsbn());
        product.setWeight(dto.getWeight());
        product.setWidth(dto.getWidth());
        product.setHeight(dto.getHeight());
        product.setDepth(dto.getDepth());
        product.setActive(dto.getActive() != null ? dto.getActive() : true);
        product.setOnSale(dto.getOnSale() != null ? dto.getOnSale() : false);
        product.setOnlineOnly(dto.getOnlineOnly() != null ? dto.getOnlineOnly() : false);
        product.setMetaTitle(dto.getMetaTitle());
        product.setMetaDescription(dto.getMetaDescription());

        product.setVisibility(dto.getVisibility() != null
                ? Product.Visibility.valueOf(dto.getVisibility())
                : Product.Visibility.BOTH);
        if (dto.getCondition() != null) {
            product.setCondition(Product.ProductCondition.valueOf(dto.getCondition()));
        }
        if (dto.getProductType() != null) {
            product.setProductType(Product.ProductType.valueOf(dto.getProductType()));
        }

        // Set categories
        if (dto.getDefaultCategoryId() != null) {
            categoryRepository.findById(dto.getDefaultCategoryId())
                    .ifPresent(product::setDefaultCategory);
        }
        if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
            product.setCategories(new HashSet<>(categoryRepository.findAllById(dto.getCategoryIds())));
        }
    }

    private void mapUpdateDtoToEntity(ProductUpdateDto dto, Product product) {
        if (dto.getName() != null) product.setName(dto.getName());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getDescriptionShort() != null) product.setDescriptionShort(dto.getDescriptionShort());
        if (dto.getLinkRewrite() != null) product.setLinkRewrite(dto.getLinkRewrite());
        if (dto.getPrice() != null) product.setPrice(dto.getPrice());
        if (dto.getWholesalePrice() != null) product.setWholesalePrice(dto.getWholesalePrice());
        if (dto.getQuantity() != null) product.setQuantity(dto.getQuantity());
        if (dto.getMinimalQuantity() != null) product.setMinimalQuantity(dto.getMinimalQuantity());
        if (dto.getReference() != null) product.setReference(dto.getReference());
        if (dto.getEan13() != null) product.setEan13(dto.getEan13());
        if (dto.getIsbn() != null) product.setIsbn(dto.getIsbn());
        if (dto.getWeight() != null) product.setWeight(dto.getWeight());
        if (dto.getWidth() != null) product.setWidth(dto.getWidth());
        if (dto.getHeight() != null) product.setHeight(dto.getHeight());
        if (dto.getDepth() != null) product.setDepth(dto.getDepth());
        if (dto.getActive() != null) product.setActive(dto.getActive());
        if (dto.getOnSale() != null) product.setOnSale(dto.getOnSale());
        if (dto.getOnlineOnly() != null) product.setOnlineOnly(dto.getOnlineOnly());
        if (dto.getMetaTitle() != null) product.setMetaTitle(dto.getMetaTitle());
        if (dto.getMetaDescription() != null) product.setMetaDescription(dto.getMetaDescription());

        if (dto.getVisibility() != null) {
            product.setVisibility(Product.Visibility.valueOf(dto.getVisibility()));
        }
        if (dto.getCondition() != null) {
            product.setCondition(Product.ProductCondition.valueOf(dto.getCondition()));
        }
        if (dto.getProductType() != null) {
            product.setProductType(Product.ProductType.valueOf(dto.getProductType()));
        }

        if (dto.getDefaultCategoryId() != null) {
            categoryRepository.findById(dto.getDefaultCategoryId())
                    .ifPresent(product::setDefaultCategory);
        }
        if (dto.getCategoryIds() != null) {
            product.setCategories(new HashSet<>(categoryRepository.findAllById(dto.getCategoryIds())));
        }
    }

    private String generateLinkRewrite(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}
