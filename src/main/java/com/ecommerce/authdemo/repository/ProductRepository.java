package com.ecommerce.authdemo.repository;

import com.ecommerce.authdemo.entity.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    // CATEGORY PRODUCTS
    List<Product> findByCategoryIdAndStatus(Long categoryId, String status);

    // SUBCATEGORY PRODUCTS
    List<Product> findBySubcategoryIdAndStatus(Long subcategoryId, String status);

    // SEARCH PRODUCTS
    List<Product> findByNameContainingIgnoreCaseAndStatus(String keyword, String status);

    // PAGINATED CATEGORY PRODUCTS
    Page<Product> findByCategoryIdAndStatus(Long categoryId, String status, Pageable pageable);

    // PAGINATED SUBCATEGORY PRODUCTS
    Page<Product> findBySubcategoryIdAndStatus(Long subcategoryId, String status, Pageable pageable);

}