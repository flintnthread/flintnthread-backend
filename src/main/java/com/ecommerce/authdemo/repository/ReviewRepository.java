package com.ecommerce.authdemo.repository;

import com.ecommerce.authdemo.entity.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<ProductReview, Long> {
    List<ProductReview> findByProduct_IdOrderByCreatedAtDesc(Long productId);

    List<ProductReview> findByProduct_IdAndStatusTrueOrderByCreatedAtDesc(Long productId);

    Page<ProductReview> findByProduct_IdOrderByCreatedAtDesc(Long productId, Pageable pageable);

    Page<ProductReview> findByProduct_IdAndStatusTrueOrderByCreatedAtDesc(Long productId, Pageable pageable);
}
