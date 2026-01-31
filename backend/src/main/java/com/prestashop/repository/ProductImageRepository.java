package com.prestashop.repository;

import com.prestashop.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductIdOrderByPositionAsc(Long productId);

    Optional<ProductImage> findByProductIdAndCoverTrue(Long productId);

    @Query("SELECT MAX(pi.position) FROM ProductImage pi WHERE pi.product.id = :productId")
    Integer findMaxPositionByProductId(@Param("productId") Long productId);

    @Modifying
    @Query("UPDATE ProductImage pi SET pi.cover = false WHERE pi.product.id = :productId")
    void clearCoverByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(pi) FROM ProductImage pi WHERE pi.product.id = :productId")
    long countByProductId(@Param("productId") Long productId);

    @Query("SELECT pi FROM ProductImage pi WHERE pi.s3Url IS NULL OR pi.s3Url = ''")
    List<ProductImage> findImagesWithoutS3Url();
}
