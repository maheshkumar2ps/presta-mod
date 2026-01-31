package com.prestashop.controller;

import com.prestashop.dto.ApiResponse;
import com.prestashop.dto.CategoryDto;
import com.prestashop.dto.ProductDto;
import com.prestashop.service.CategoryService;
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
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Public category endpoints")
public class CategoryController {

    private final CategoryService categoryService;
    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Get category tree", description = "Get hierarchical category tree")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getCategoryTree() {
        List<CategoryDto> categories = categoryService.getCategoryTree();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/flat")
    @Operation(summary = "Get all categories", description = "Get flat list of all categories")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getAllCategories() {
        List<CategoryDto> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get category", description = "Get category details by slug")
    public ResponseEntity<ApiResponse<CategoryDto>> getCategory(@PathVariable String slug) {
        CategoryDto category = categoryService.getCategoryBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(category));
    }

    @GetMapping("/{slug}/products")
    @Operation(summary = "Get category products", description = "Get products in a category (PLP)")
    public ResponseEntity<ApiResponse<Page<ProductDto>>> getCategoryProducts(
            @PathVariable String slug,
            @PageableDefault(size = 20, sort = "dateAdd", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProductDto> products = productService.getProductsByCategorySlug(slug, pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/{slug}/children")
    @Operation(summary = "Get child categories", description = "Get direct child categories")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getChildCategories(@PathVariable String slug) {
        CategoryDto category = categoryService.getCategoryBySlug(slug);
        List<CategoryDto> children = categoryService.getChildCategories(category.getId());
        return ResponseEntity.ok(ApiResponse.success(children));
    }
}
