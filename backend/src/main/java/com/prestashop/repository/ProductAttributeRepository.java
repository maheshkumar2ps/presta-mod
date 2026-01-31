package com.prestashop.repository;

import com.prestashop.entity.ProductAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, Long> {

    List<ProductAttribute> findByProductId(Long productId);

    Optional<ProductAttribute> findByProductIdAndDefaultOnTrue(Long productId);

    @Query("SELECT pa FROM ProductAttribute pa WHERE pa.product.id = :productId AND pa.quantity > 0")
    List<ProductAttribute> findInStockByProductId(@Param("productId") Long productId);

    @Query("SELECT SUM(pa.quantity) FROM ProductAttribute pa WHERE pa.product.id = :productId")
    Integer getTotalQuantityByProductId(@Param("productId") Long productId);
}
