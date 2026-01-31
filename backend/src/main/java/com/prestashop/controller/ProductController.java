package com.prestashop.controller;

import com.prestashop.dto.ApiResponse;
import com.prestashop.dto.ProductAttributeDto;
import com.prestashop.dto.ProductDto;
import com.prestashop.dto.ProductImageDto;
import com.prestashop.service.ImageService;
import com.prestashop.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Public product endpoints for PLP and PDP")
public class ProductController {

    private final ProductService productService;
    private final ImageService imageService;

    @GetMapping
    @Operation(summary = "List products", description = "Get paginated list of active products (PLP)")
    public ResponseEntity<ApiResponse<Page<ProductDto>>> getProducts(
            @PageableDefault(size = 20, sort = "dateAdd", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProductDto> products = productService.getProducts(pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Search products by keyword")
    public ResponseEntity<ApiResponse<Page<ProductDto>>> searchProducts(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProductDto> products = productService.searchProducts(q, pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get product details", description = "Get full product details by slug (PDP)")
    public ResponseEntity<ApiResponse<ProductDto>> getProduct(@PathVariable String slug) {
        ProductDto product = productService.getProductBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @GetMapping("/{slug}/variants")
    @Operation(summary = "Get product variants", description = "Get product variants/combinations")
    public ResponseEntity<ApiResponse<List<ProductAttributeDto>>> getProductVariants(@PathVariable String slug) {
        ProductDto product = productService.getProductBySlug(slug);
        List<ProductAttributeDto> variants = productService.getProductVariants(product.getId());
        return ResponseEntity.ok(ApiResponse.success(variants));
    }

    @GetMapping("/{slug}/images")
    @Operation(summary = "Get product images", description = "Get all images for a product")
    public ResponseEntity<ApiResponse<List<ProductImageDto>>> getProductImages(@PathVariable String slug) {
        ProductDto product = productService.getProductBySlug(slug);
        List<ProductImageDto> images = imageService.getProductImages(product.getId());
        return ResponseEntity.ok(ApiResponse.success(images));
    }
}
