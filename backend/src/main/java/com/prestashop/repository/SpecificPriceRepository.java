package com.prestashop.repository;

import com.prestashop.entity.SpecificPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpecificPriceRepository extends JpaRepository<SpecificPrice, Long> {

    List<SpecificPrice> findByProductId(Long productId);

    @Query("SELECT sp FROM SpecificPrice sp WHERE sp.product.id = :productId " +
           "AND (sp.fromDate IS NULL OR sp.fromDate <= :now) " +
           "AND (sp.toDate IS NULL OR sp.toDate >= :now) " +
           "AND sp.fromQuantity <= :quantity " +
           "ORDER BY sp.fromQuantity DESC")
    List<SpecificPrice> findActiveByProductId(
            @Param("productId") Long productId,
            @Param("now") LocalDateTime now,
            @Param("quantity") Integer quantity);

    @Query("SELECT sp FROM SpecificPrice sp WHERE sp.product.id = :productId " +
           "AND sp.productAttribute.id = :attributeId " +
           "AND (sp.fromDate IS NULL OR sp.fromDate <= :now) " +
           "AND (sp.toDate IS NULL OR sp.toDate >= :now)")
    Optional<SpecificPrice> findActiveByProductAndAttribute(
            @Param("productId") Long productId,
            @Param("attributeId") Long attributeId,
            @Param("now") LocalDateTime now);
}
