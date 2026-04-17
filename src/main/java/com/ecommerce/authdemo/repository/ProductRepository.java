package com.ecommerce.authdemo.repository;

import com.ecommerce.authdemo.dto.ProductDTO;
import com.ecommerce.authdemo.entity.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    List<Product> findTop10ByOrderByCreatedAtDesc();

    List<Product> findBySellerId(Long sellerId);

    List<Product> findTop10ByCategoryIdAndIdNot(Long categoryId, Long id);

    // 🔥 SEARCH (DB OPTIMIZED)
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> search(String keyword);

    // 🔥 POPULAR (MOST VIEWS)
// 🔥 POPULAR (MOST VIEWS)
    @Query("""
    SELECT p FROM Product p
    JOIN p.views v
    GROUP BY p
    ORDER BY COUNT(v.id) DESC
""")
    List<Product> findPopularProducts();

    List<Product> findTop10ByOrderByIdDesc();

    Page<Product> findByNameContainingIgnoreCaseAndStatus(
            String keyword,
            String status,
            Pageable pageable
    );

    List<Product> findTop20ByNameContainingIgnoreCaseAndStatus(String keyword, String active);

    List<Product> findTop5ByNameContainingIgnoreCase(String keyword);

    List<Product> findByNameContainingIgnoreCase(String keyword);

    @Query("""
    SELECT p FROM Product p
    JOIN p.views v
    WHERE v.viewedAt >= :date
    GROUP BY p
    ORDER BY COUNT(v.id) DESC
""")
    List<Product> findTrendingProducts(@Param("date") LocalDateTime date);

    @Query("""
    SELECT DISTINCT p FROM Product p
    LEFT JOIN FETCH p.variants v
    LEFT JOIN FETCH p.images i
    WHERE p.categoryId = :categoryId
""")
    List<Product> findByCategoryFull(@Param("categoryId") Long categoryId);


    @Query("""
    SELECT DISTINCT p FROM Product p
    LEFT JOIN FETCH p.variants v
    LEFT JOIN FETCH p.images i
    WHERE p.subcategoryId = :subId
""")
    List<Product> findBySubCategoryFull(@Param("subId") Long subId);
}