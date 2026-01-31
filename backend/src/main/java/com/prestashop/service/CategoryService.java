package com.prestashop.service;

import com.prestashop.dto.CategoryCreateDto;
import com.prestashop.dto.CategoryDto;
import com.prestashop.entity.Category;
import com.prestashop.exception.ResourceNotFoundException;
import com.prestashop.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryDto> getCategoryTree() {
        return categoryRepository.findRootCategoriesWithChildren().stream()
                .map(CategoryDto::withChildren)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAllActiveOrdered().stream()
                .map(CategoryDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategoryBySlug(String slug) {
        Category category = categoryRepository.findByLinkRewrite(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + slug));
        return CategoryDto.withBreadcrumb(category);
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        return CategoryDto.withBreadcrumb(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getChildCategories(Long parentId) {
        return categoryRepository.findByParentId(parentId).stream()
                .map(CategoryDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryDto createCategory(CategoryCreateDto createDto) {
        Category category = new Category();
        category.setName(createDto.getName());
        category.setDescription(createDto.getDescription());
        category.setActive(createDto.getActive() != null ? createDto.getActive() : true);
        category.setMetaTitle(createDto.getMetaTitle());
        category.setMetaDescription(createDto.getMetaDescription());

        // Generate link_rewrite
        if (createDto.getLinkRewrite() != null && !createDto.getLinkRewrite().isBlank()) {
            category.setLinkRewrite(createDto.getLinkRewrite());
        } else {
            category.setLinkRewrite(generateLinkRewrite(createDto.getName()));
        }

        // Ensure unique link_rewrite
        String baseSlug = category.getLinkRewrite();
        int counter = 1;
        while (categoryRepository.existsByLinkRewrite(category.getLinkRewrite())) {
            category.setLinkRewrite(baseSlug + "-" + counter++);
        }

        // Set parent
        if (createDto.getParentId() != null) {
            Category parent = categoryRepository.findById(createDto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
            category.setParent(parent);
            category.setLevelDepth(parent.getLevelDepth() + 1);
        } else {
            category.setLevelDepth(0);
            category.setIsRootCategory(true);
        }

        // Set position
        if (createDto.getPosition() != null) {
            category.setPosition(createDto.getPosition());
        } else {
            Integer maxPosition;
            if (createDto.getParentId() != null) {
                maxPosition = categoryRepository.findMaxPositionByParentId(createDto.getParentId());
            } else {
                maxPosition = categoryRepository.findMaxPositionForRootCategories();
            }
            category.setPosition(maxPosition != null ? maxPosition + 1 : 0);
        }

        category = categoryRepository.save(category);
        return CategoryDto.fromEntity(category);
    }

    @Transactional
    public CategoryDto updateCategory(Long id, CategoryCreateDto updateDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));

        if (updateDto.getName() != null) {
            category.setName(updateDto.getName());
        }
        if (updateDto.getDescription() != null) {
            category.setDescription(updateDto.getDescription());
        }
        if (updateDto.getLinkRewrite() != null) {
            // Check uniqueness if changing
            if (!category.getLinkRewrite().equals(updateDto.getLinkRewrite())
                    && categoryRepository.existsByLinkRewrite(updateDto.getLinkRewrite())) {
                throw new IllegalArgumentException("Link rewrite already exists");
            }
            category.setLinkRewrite(updateDto.getLinkRewrite());
        }
        if (updateDto.getActive() != null) {
            category.setActive(updateDto.getActive());
        }
        if (updateDto.getPosition() != null) {
            category.setPosition(updateDto.getPosition());
        }
        if (updateDto.getMetaTitle() != null) {
            category.setMetaTitle(updateDto.getMetaTitle());
        }
        if (updateDto.getMetaDescription() != null) {
            category.setMetaDescription(updateDto.getMetaDescription());
        }

        // Handle parent change
        if (updateDto.getParentId() != null &&
            (category.getParent() == null || !category.getParent().getId().equals(updateDto.getParentId()))) {
            Category newParent = categoryRepository.findById(updateDto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
            category.setParent(newParent);
            category.setLevelDepth(newParent.getLevelDepth() + 1);
            category.setIsRootCategory(false);
        } else if (updateDto.getParentId() == null && category.getParent() != null) {
            category.setParent(null);
            category.setLevelDepth(0);
            category.setIsRootCategory(true);
        }

        category = categoryRepository.save(category);
        return CategoryDto.fromEntity(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));

        // Check if category has children
        if (!category.getChildren().isEmpty()) {
            throw new IllegalStateException("Cannot delete category with children");
        }

        // Check if category has products
        if (!category.getProducts().isEmpty()) {
            throw new IllegalStateException("Cannot delete category with products");
        }

        categoryRepository.delete(category);
    }

    private String generateLinkRewrite(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}
