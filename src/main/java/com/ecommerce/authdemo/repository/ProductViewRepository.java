package com.ecommerce.authdemo.repository;

import com.ecommerce.authdemo.entity.ProductView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

    public interface ProductViewRepository extends JpaRepository<ProductView, Long> {

        List<ProductView> findTop10ByUserIdOrderByViewedAtDesc(Long userId);

        List<ProductView> findTop10BySessionIdOrderByViewedAtDesc(String sessionId);

        @Query(value = """
            SELECT product_id
            FROM product_views
            GROUP BY product_id
            ORDER BY COUNT(*) DESC
            LIMIT 10
            """, nativeQuery = true)
        List<Long> findMostViewedProductIds();
    }

