package com.prestashop.repository;

import com.prestashop.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByLinkRewrite(String linkRewrite);

    @Query("SELECT c FROM Category c WHERE c.active = true AND c.parent IS NULL ORDER BY c.position")
    List<Category> findRootCategories();

    @Query("SELECT c FROM Category c WHERE c.active = true AND c.parent.id = :parentId ORDER BY c.position")
    List<Category> findByParentId(@Param("parentId") Long parentId);

    @Query("SELECT c FROM Category c WHERE c.active = true ORDER BY c.levelDepth, c.position")
    List<Category> findAllActiveOrdered();

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.children WHERE c.active = true AND c.parent IS NULL ORDER BY c.position")
    List<Category> findRootCategoriesWithChildren();

    boolean existsByLinkRewrite(String linkRewrite);

    @Query("SELECT MAX(c.position) FROM Category c WHERE c.parent.id = :parentId")
    Integer findMaxPositionByParentId(@Param("parentId") Long parentId);

    @Query("SELECT MAX(c.position) FROM Category c WHERE c.parent IS NULL")
    Integer findMaxPositionForRootCategories();
}
