package com.ecommerce.authdemo.repository;

import com.ecommerce.authdemo.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    // ---------------- BASIC ----------------
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    List<Product> findTop10ByOrderByCreatedAtDesc();

    List<Product> findBySellerId(Long sellerId);

    List<Product> findTop10ByCategoryIdAndIdNot(Long categoryId, Long id);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findTop20ByNameContainingIgnoreCaseAndStatus(String name, String status);

    List<Product> findTop5ByNameContainingIgnoreCase(String name);

    List<Product> findTop10ByOrderByIdDesc();

    Page<Product> findByNameContainingIgnoreCaseAndStatus(String name, String status, Pageable pageable);

    // ---------------- SEARCH ----------------
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> search(@Param("keyword") String keyword);

    // ---------------- POPULAR ----------------
    @Query("""
        SELECT p FROM Product p
        JOIN p.views v
        GROUP BY p
        ORDER BY COUNT(v.id) DESC
    """)
    List<Product> findPopularProducts();

    @Query(value = """
        SELECT p.*
        FROM products p
        WHERE p.category_id = :mainCategoryId
           OR p.category_id IN (
               SELECT c.id FROM categories c WHERE c.parent_id = :mainCategoryId
           )
        ORDER BY p.created_at DESC
        LIMIT 10
    """, nativeQuery = true)
    List<Product> findRecentProductsByMainCategory(@Param("mainCategoryId") Long mainCategoryId);

    @Query(value = """
        SELECT p.*
        FROM products p
        JOIN product_views v ON v.product_id = p.id
        WHERE p.category_id = :mainCategoryId
           OR p.category_id IN (
               SELECT c.id FROM categories c WHERE c.parent_id = :mainCategoryId
           )
        GROUP BY p.id
        ORDER BY COUNT(v.id) DESC
        LIMIT 10
    """, nativeQuery = true)
    List<Product> findPopularProductsByMainCategory(@Param("mainCategoryId") Long mainCategoryId);

    // ---------------- TRENDING ----------------
    @Query("""
        SELECT p FROM Product p
        JOIN p.views v
        WHERE v.viewedAt >= :date
        GROUP BY p
        ORDER BY COUNT(v.id) DESC
    """)
    List<Product> findTrendingProducts(@Param("date") LocalDateTime date);

    @Query(value = """
        SELECT p.*
        FROM products p
        JOIN product_views v ON v.product_id = p.id
        WHERE v.viewed_at >= :date
          AND (
               p.category_id = :mainCategoryId
               OR p.category_id IN (
                   SELECT c.id FROM categories c WHERE c.parent_id = :mainCategoryId
               )
          )
        GROUP BY p.id
        ORDER BY COUNT(v.id) DESC
        LIMIT 10
    """, nativeQuery = true)
    List<Product> findTrendingProductsByMainCategory(@Param("date") LocalDateTime date, @Param("mainCategoryId") Long mainCategoryId);

    @Query(value = """
        SELECT p.*
        FROM products p
        WHERE p.category_id = :mainCategoryId
           OR p.category_id IN (
               SELECT c.id FROM categories c WHERE c.parent_id = :mainCategoryId
           )
        ORDER BY p.created_at DESC
        LIMIT 10
    """, nativeQuery = true)
    List<Product> findRelatedProductsByMainCategory(@Param("mainCategoryId") Long mainCategoryId);

    // ---------------- FULL FETCH ----------------
    @Query("""
        SELECT DISTINCT p FROM Product p
        LEFT JOIN FETCH p.variants
        LEFT JOIN FETCH p.images
        WHERE p.categoryId = :categoryId
    """)
    List<Product> findByCategoryFull(@Param("categoryId") Long categoryId);

    @Query("""
        SELECT DISTINCT p FROM Product p
        LEFT JOIN FETCH p.variants
        LEFT JOIN FETCH p.images
        WHERE p.subcategoryId = :subId
    """)
    List<Product> findBySubCategoryFull(@Param("subId") Long subId);

    // ---------------- TOP BY SELLING PRICE (ALL CATEGORIES) ----------------
    @Query(value = """
        SELECT p.*
        FROM products p
        JOIN product_variants v ON p.id = v.product_id
        GROUP BY p.id
        ORDER BY MAX(v.selling_price) DESC
        LIMIT 10
    """, nativeQuery = true)
    List<Product> findTopSellingProducts();

    // ---------------- TOP PRODUCTS BY CATEGORY ----------------
    @Query(value = """
        SELECT p.* 
        FROM products p
        JOIN product_variants v ON p.id = v.product_id
        WHERE p.category_id = :categoryId
        GROUP BY p.id
        ORDER BY MAX(v.selling_price) DESC
        LIMIT 10
    """, nativeQuery = true)
    List<Product> findTopProductsByCategory(@Param("categoryId") Long categoryId);


    @Query(value = """
    SELECT p.* 
    FROM products p
    JOIN product_variants v ON p.id = v.product_id
    GROUP BY p.id
    ORDER BY MAX(v.discount_percentage) DESC
    LIMIT 10
""", nativeQuery = true)
    List<Product> findTopDiscountProducts();

    @Query(value = """
        SELECT p.*
        FROM products p
        JOIN product_variants v ON p.id = v.product_id
        WHERE p.category_id = :mainCategoryId
           OR p.category_id IN (
               SELECT c.id FROM categories c WHERE c.parent_id = :mainCategoryId
           )
        GROUP BY p.id
        ORDER BY MIN(v.discount_percentage) ASC
    """, nativeQuery = true)
    List<Product> findDiscountProductsByMainCategoryAsc(@Param("mainCategoryId") Long mainCategoryId);

    // ---------------- PRODUCTS BY MAIN CATEGORY (INCLUDING SUBCATEGORIES) ----------------
    @Query(value = """
        SELECT DISTINCT p.* 
        FROM products p
        WHERE p.category_id = :mainCategoryId
        OR p.category_id IN (
            SELECT c.id FROM categories c WHERE c.parent_id = :mainCategoryId
        )
    """, nativeQuery = true)
    List<Product> findByMainCategory(@Param("mainCategoryId") Long mainCategoryId);

    @Query(value = """
        SELECT DISTINCT p.* 
        FROM products p
        LEFT JOIN product_variants v ON p.id = v.product_id
        WHERE p.category_id = :mainCategoryId
        OR p.category_id IN (
            SELECT c.id FROM categories c WHERE c.parent_id = :mainCategoryId
        )
    """, nativeQuery = true)
    List<Product> findByMainCategoryFull(@Param("mainCategoryId") Long mainCategoryId);
}