package com.prestashop.repository;

import com.prestashop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findByLinkRewrite(String linkRewrite);

    // Show active products visible on storefront (BOTH, CATALOG, SEARCH - excludes NONE)
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.visibility IN ('BOTH', 'CATALOG', 'SEARCH')")
    Page<Product> findAllActive(Pageable pageable);

    @Query("SELECT p FROM Product p JOIN p.categories c WHERE c.id = :categoryId AND p.active = true AND p.visibility IN ('BOTH', 'CATALOG', 'SEARCH')")
    Page<Product> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.visibility IN ('BOTH', 'CATALOG', 'SEARCH') " +
           "AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.reference) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> search(@Param("search") String search, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.onSale = true")
    List<Product> findOnSale();

    @Query("SELECT p FROM Product p WHERE p.active = true ORDER BY p.dateAdd DESC")
    Page<Product> findNewProducts(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.defaultCategory.id = :categoryId")
    List<Product> findByDefaultCategoryId(@Param("categoryId") Long categoryId);

    boolean existsByLinkRewrite(String linkRewrite);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.active = true")
    long countActive();
}
