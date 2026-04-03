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

    // PAGINATION - CATEGORY PRODUCTS
    Page<Product> findByCategoryIdAndStatus(Long categoryId, String status, Pageable pageable);

    // PAGINATION - SUBCATEGORY PRODUCTS
    Page<Product> findBySubcategoryIdAndStatus(Long subcategoryId, String status, Pageable pageable);

    /*
    -----------------------------------------
    🔎 SEARCH USING PRODUCT NAME
    -----------------------------------------
    */

    List<Product> findByNameContainingIgnoreCase(String keyword);

    List<Product> findTop5ByNameContainingIgnoreCase(String keyword);

    List<Product> findTop20ByNameContainingIgnoreCase(String keyword);

    List<Product> findTop20ByNameContainingIgnoreCaseAndStatus(String keyword, String status);

    Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseAndStatus(
            String keyword,
            String status,
            Pageable pageable
    );

    List<Product> findByNameContainingIgnoreCaseAndStatus(String name, String status);

    /*
    -----------------------------------------
    🔥 TRENDING PRODUCTS
    -----------------------------------------
    */

    List<Product> findTop10ByOrderByIdDesc();

}