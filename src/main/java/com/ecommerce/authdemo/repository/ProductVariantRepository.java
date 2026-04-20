package com.ecommerce.authdemo.repository;

import com.ecommerce.authdemo.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

    public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

        List<ProductVariant> findByProductId(Long productId);

        @Query("SELECT DISTINCT pv.size FROM ProductVariant pv WHERE pv.size IS NOT NULL AND pv.size != ''")
        List<String> findAllDistinctSizes();

        @Query("SELECT DISTINCT pv.color FROM ProductVariant pv WHERE pv.color IS NOT NULL AND pv.color != ''")
        List<String> findAllDistinctColors();

        @Query("SELECT DISTINCT pv.size FROM ProductVariant pv WHERE pv.product.id = :productId AND pv.size IS NOT NULL AND pv.size != ''")
        List<String> findDistinctSizesByProductId(Long productId);

        @Query("SELECT DISTINCT pv.color FROM ProductVariant pv WHERE pv.product.id = :productId AND pv.color IS NOT NULL AND pv.color != ''")
        List<String> findDistinctColorsByProductId(Long productId);

    }

