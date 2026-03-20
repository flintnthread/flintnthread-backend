package com.ecommerce.authdemo.service;

import com.ecommerce.authdemo.dto.CategoryWithSubDTO;
import com.ecommerce.authdemo.dto.SubCategoryResponseDTO;
import com.ecommerce.authdemo.entity.Category;
import com.ecommerce.authdemo.dto.CategoryTreeDTO;

import java.util.List;
import java.util.Map;

public interface CategoryService {

    // Get all root categories
    List<Category> getMainCategories();

    // Get sub categories using parentId
    List<Category> getSubCategories(Long parentId);

    // Get category by id
    Category getCategory(Long id);

    // Category tree structure
    List<CategoryTreeDTO> getCategoryTree();

    // Search categories
    List<Category> searchCategories(String keyword);

    // Get subcategories from SubCategory table
    List<CategoryWithSubDTO> getSubCategoriesFromTable(Long categoryId);

    // Search both categories and products
    Map<String, Object> searchAll(String keyword);
}