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

    List<Product> findTop60ByStatusOrderByCreatedAtDesc(String status);
    List<Product> findTop300ByStatusOrderByCreatedAtDesc(String status);

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
        WHERE p.category_id = :mainCategoryId
           OR p.category_id IN (
               SELECT c.id FROM categories c WHERE c.parent_id = :mainCategoryId
           )
        ORDER BY p.created_at DESC
        LIMIT 10
    """, nativeQuery = true)
    List<Product> findLatestProductsByMainCategory(@Param("mainCategoryId") Long mainCategoryId);

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

    @Query(value = """
        SELECT p.*
        FROM products p
        LEFT JOIN product_variants v ON v.product_id = p.id
        LEFT JOIN product_views pv ON pv.product_id = p.id
        WHERE p.category_id = :mainCategoryId
           OR p.category_id IN (
               SELECT c.id FROM categories c WHERE c.parent_id = :mainCategoryId
           )
        GROUP BY p.id
        ORDER BY MAX(COALESCE(v.discount_percentage, 0)) DESC,
                 COUNT(pv.id) DESC,
                 p.created_at DESC
        LIMIT 10
    """, nativeQuery = true)
    List<Product> findSpotlightProductsByMainCategory(@Param("mainCategoryId") Long mainCategoryId);

    @Query(value = """
        SELECT p.*
        FROM products p
        JOIN (
            SELECT MIN(p2.id) AS id
            FROM products p2
            WHERE p2.category_id = :mainCategoryId
               OR p2.category_id IN (
                   SELECT c.id FROM categories c WHERE c.parent_id = :mainCategoryId
               )
            GROUP BY LOWER(TRIM(p2.name))
        ) uniq ON uniq.id = p.id
        ORDER BY p.created_at DESC
        LIMIT 10
    """, nativeQuery = true)
    List<Product> findUniqueProductsByMainCategory(@Param("mainCategoryId") Long mainCategoryId);

    @Query(value = """
        SELECT p.*
        FROM products p
        JOIN order_items oi ON oi.product_id = p.id
        WHERE p.category_id = :mainCategoryId
           OR p.category_id IN (
               SELECT c.id FROM categories c WHERE c.parent_id = :mainCategoryId
           )
        GROUP BY p.id
        ORDER BY SUM(oi.quantity) DESC, MAX(oi.created_at) DESC
        LIMIT 10
    """, nativeQuery = true)
    List<Product> findTopCollectionsByMainCategory(@Param("mainCategoryId") Long mainCategoryId);

    @Query(value = """
        SELECT p.*
        FROM products p
        LEFT JOIN product_views popularity ON popularity.product_id = p.id
        LEFT JOIN product_variants v ON v.product_id = p.id
        WHERE (
               p.category_id = :mainCategoryId
               OR p.category_id IN (
                   SELECT c.id FROM categories c WHERE c.parent_id = :mainCategoryId
               )
        )
          AND (
               (:userId IS NULL AND (:sessionId IS NULL OR :sessionId = ''))
               OR NOT EXISTS (
                   SELECT 1
                   FROM product_views seen
                   WHERE seen.product_id = p.id
                     AND (
                         (:userId IS NOT NULL AND seen.user_id = :userId)
                         OR (:sessionId IS NOT NULL AND :sessionId <> '' AND seen.session_id = :sessionId)
                     )
               )
          )
        GROUP BY p.id
        ORDER BY COUNT(popularity.id) DESC,
                 MAX(COALESCE(v.discount_percentage, 0)) DESC,
                 p.created_at DESC
        LIMIT 10
    """, nativeQuery = true)
    List<Product> findRecommendedProductsByMainCategory(
            @Param("mainCategoryId") Long mainCategoryId,
            @Param("userId") Long userId,
            @Param("sessionId") String sessionId
    );

    @Query(value = """
        SELECT pv.product_id
        FROM product_views pv
        JOIN products p ON p.id = pv.product_id
        WHERE (
               p.category_id = :mainCategoryId
               OR p.category_id IN (
                   SELECT c.id FROM categories c WHERE c.parent_id = :mainCategoryId
               )
        )
          AND (
               (:userId IS NOT NULL AND pv.user_id = :userId)
               OR (:sessionId IS NOT NULL AND :sessionId <> '' AND pv.session_id = :sessionId)
          )
        GROUP BY pv.product_id
        ORDER BY MAX(pv.viewed_at) DESC
        LIMIT 10
    """, nativeQuery = true)
    List<Long> findRecentlyViewedProductIdsByMainCategory(
            @Param("mainCategoryId") Long mainCategoryId,
            @Param("userId") Long userId,
            @Param("sessionId") String sessionId
    );

    @Query(value = """
        SELECT pv.product_id
        FROM product_views pv
        WHERE (
               (:userId IS NOT NULL AND pv.user_id = :userId)
               OR (:sessionId IS NOT NULL AND :sessionId <> '' AND pv.session_id = :sessionId)
        )
        GROUP BY pv.product_id
        ORDER BY MAX(pv.viewed_at) DESC
        LIMIT 10
    """, nativeQuery = true)
    List<Long> findRecentlyViewedProductIds(
            @Param("userId") Long userId,
            @Param("sessionId") String sessionId
    );

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

    @Query(value = """
        SELECT p.*
        FROM products p
        JOIN product_variants v ON p.id = v.product_id
        WHERE (
               p.category_id = :mainCategoryId
               OR p.category_id IN (
                   SELECT c.id FROM categories c WHERE c.parent_id = :mainCategoryId
               )
        )
          AND ROUND(COALESCE(v.discount_percentage, 0), 2) = ROUND(:discountPercentage, 2)
        GROUP BY p.id
        ORDER BY p.created_at DESC
    """, nativeQuery = true)
    List<Product> findProductsByMainCategoryAndExactDiscountPercentage(
            @Param("mainCategoryId") Long mainCategoryId,
            @Param("discountPercentage") Double discountPercentage
    );

    @Query(value = """
        SELECT p.*
        FROM products p
        JOIN product_variants v ON p.id = v.product_id
        WHERE (
               p.category_id = :mainCategoryId
               OR p.category_id IN (
                   SELECT c.id FROM categories c WHERE c.parent_id = :mainCategoryId
               )
        )
          AND COALESCE(v.discount_percentage, 0) <= :maxDiscountPercentage
        GROUP BY p.id
        ORDER BY MAX(COALESCE(v.discount_percentage, 0)) DESC, p.created_at DESC
    """, nativeQuery = true)
    List<Product> findProductsByMainCategoryWithDiscountLessThanEqual(
            @Param("mainCategoryId") Long mainCategoryId,
            @Param("maxDiscountPercentage") Double maxDiscountPercentage
    );

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