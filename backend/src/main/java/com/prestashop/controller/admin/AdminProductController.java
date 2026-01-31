package com.prestashop.controller.admin;

import com.prestashop.dto.*;
import com.prestashop.service.ImageService;
import com.prestashop.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@Tag(name = "Admin - Products", description = "Admin product management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AdminProductController {

    private final ProductService productService;
    private final ImageService imageService;

    @GetMapping
    @Operation(summary = "List all products", description = "Get paginated list of all products (including inactive)")
    public ResponseEntity<ApiResponse<Page<ProductDto>>> getProducts(
            @PageableDefault(size = 20, sort = "dateUpd", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProductDto> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product", description = "Get product details by ID")
    public ResponseEntity<ApiResponse<ProductDto>> getProduct(@PathVariable Long id) {
        ProductDto product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @PostMapping
    @Operation(summary = "Create product", description = "Create a new product")
    public ResponseEntity<ApiResponse<ProductDto>> createProduct(@Valid @RequestBody ProductCreateDto createDto) {
        ProductDto product = productService.createProduct(createDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(product, "Product created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Update an existing product")
    public ResponseEntity<ApiResponse<ProductDto>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateDto updateDto) {
        ProductDto product = productService.updateProduct(id, updateDto);
        return ResponseEntity.ok(ApiResponse.success(product, "Product updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product", description = "Delete a product")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Product deleted successfully"));
    }

    // Bulk operations

    @PatchMapping("/bulk/status")
    @Operation(summary = "Bulk update status", description = "Activate or deactivate multiple products")
    public ResponseEntity<ApiResponse<Void>> bulkUpdateStatus(
            @RequestParam List<Long> ids,
            @RequestParam boolean active) {
        productService.bulkUpdateStatus(ids, active);
        return ResponseEntity.ok(ApiResponse.success(null, "Products updated successfully"));
    }

    @DeleteMapping("/bulk")
    @Operation(summary = "Bulk delete", description = "Delete multiple products")
    public ResponseEntity<ApiResponse<Void>> bulkDelete(@RequestParam List<Long> ids) {
        productService.bulkDelete(ids);
        return ResponseEntity.ok(ApiResponse.success(null, "Products deleted successfully"));
    }

    // Variant management

    @GetMapping("/{id}/variants")
    @Operation(summary = "Get variants", description = "Get product variants")
    public ResponseEntity<ApiResponse<List<ProductAttributeDto>>> getVariants(@PathVariable Long id) {
        List<ProductAttributeDto> variants = productService.getProductVariants(id);
        return ResponseEntity.ok(ApiResponse.success(variants));
    }

    @PostMapping("/{id}/variants")
    @Operation(summary = "Add variant", description = "Add a product variant")
    public ResponseEntity<ApiResponse<ProductAttributeDto>> addVariant(
            @PathVariable Long id,
            @Valid @RequestBody ProductAttributeCreateDto createDto) {
        ProductAttributeDto variant = productService.addVariant(id, createDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(variant, "Variant added successfully"));
    }

    @DeleteMapping("/{id}/variants/{variantId}")
    @Operation(summary = "Delete variant", description = "Delete a product variant")
    public ResponseEntity<ApiResponse<Void>> deleteVariant(
            @PathVariable Long id,
            @PathVariable Long variantId) {
        productService.deleteVariant(id, variantId);
        return ResponseEntity.ok(ApiResponse.success(null, "Variant deleted successfully"));
    }

    // Image management

    @GetMapping("/{id}/images")
    @Operation(summary = "Get images", description = "Get product images")
    public ResponseEntity<ApiResponse<List<ProductImageDto>>> getImages(@PathVariable Long id) {
        List<ProductImageDto> images = imageService.getProductImages(id);
        return ResponseEntity.ok(ApiResponse.success(images));
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload image", description = "Upload a product image")
    public ResponseEntity<ApiResponse<ProductImageDto>> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String legend,
            @RequestParam(defaultValue = "false") boolean cover) throws IOException {
        ProductImageDto image = imageService.uploadImage(id, file, legend, cover);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(image, "Image uploaded successfully"));
    }

    @DeleteMapping("/images/{imageId}")
    @Operation(summary = "Delete image", description = "Delete a product image")
    public ResponseEntity<ApiResponse<Void>> deleteImage(@PathVariable Long imageId) {
        imageService.deleteImage(imageId);
        return ResponseEntity.ok(ApiResponse.success(null, "Image deleted successfully"));
    }

    @PatchMapping("/images/{imageId}/cover")
    @Operation(summary = "Set cover image", description = "Set an image as the cover image")
    public ResponseEntity<ApiResponse<ProductImageDto>> setCover(@PathVariable Long imageId) {
        ProductImageDto image = imageService.setCover(imageId);
        return ResponseEntity.ok(ApiResponse.success(image, "Cover image set successfully"));
    }
}
