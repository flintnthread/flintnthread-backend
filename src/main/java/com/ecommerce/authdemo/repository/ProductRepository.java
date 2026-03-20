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

    // SEARCH PRODUCTS WITH STATUS
    List<Product> findByProductNameContainingIgnoreCaseAndStatus(String keyword, String status);

    // PAGINATION - CATEGORY PRODUCTS
    Page<Product> findByCategoryIdAndStatus(Long categoryId, String status, Pageable pageable);

    // PAGINATION - SUBCATEGORY PRODUCTS
    Page<Product> findBySubcategoryIdAndStatus(Long subcategoryId, String status, Pageable pageable);

    // GENERAL SEARCH (HOME SEARCH BAR)
    List<Product> findByProductNameContainingIgnoreCase(String keyword);
}