package com.ecommerce.authdemo.service;

import com.ecommerce.authdemo.entity.Category;
import com.ecommerce.authdemo.dto.CategoryTreeDTO;

import java.util.List;

public interface CategoryService {

    List<Category> getMainCategories();

    List<Category> getSubCategories(Long parentId);

    Category getCategory(Long id);

    List<CategoryTreeDTO> getCategoryTree();

    List<Category> searchCategories(String keyword);

}