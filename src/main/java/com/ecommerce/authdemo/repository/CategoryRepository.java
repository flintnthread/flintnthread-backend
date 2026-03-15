package com.ecommerce.authdemo.repository;

import com.ecommerce.authdemo.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // main categories
    List<Category> findByParentIdIsNull();

    // subcategories
    List<Category> findByParentId(Long parentId);

    // search
    List<Category> findByCategoryNameContainingIgnoreCase(String name);

}