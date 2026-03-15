package com.ecommerce.authdemo.service.impl;

import com.ecommerce.authdemo.entity.Category;
import com.ecommerce.authdemo.repository.CategoryRepository;
import com.ecommerce.authdemo.service.CategoryService;
import com.ecommerce.authdemo.dto.CategoryTreeDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Get all main categories
     */
    @Override
    public List<Category> getMainCategories() {

        return categoryRepository.findByParentIdIsNull()
                .stream()
                .filter(c -> c.getStatus() != null && c.getStatus() == 1)
                .sorted(Comparator.comparing(Category::getCategoryName))
                .toList();
    }

    /**
     * Get sub categories by parent id
     */
    @Override
    public List<Category> getSubCategories(Long parentId) {

        if (parentId == null) {
            throw new IllegalArgumentException("Parent ID cannot be null");
        }

        return categoryRepository.findByParentId(parentId)
                .stream()
                .filter(c -> c.getStatus() != null && c.getStatus() == 1)
                .sorted(Comparator.comparing(Category::getCategoryName))
                .toList();
    }

    /**
     * Get category by id
     */
    @Override
    public Category getCategory(Long id) {

        if (id == null) {
            throw new IllegalArgumentException("Category ID cannot be null");
        }

        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Category not found with id: " + id));
    }

    /**
     * Get full category tree
     */
    @Override
    public List<CategoryTreeDTO> getCategoryTree() {

        List<Category> allCategories = categoryRepository.findAll();

        Map<Long, List<Category>> categoryMap =
                allCategories.stream()
                        .collect(Collectors.groupingBy(
                                c -> c.getParentId() == null ? 0L : c.getParentId()
                        ));

        List<Category> rootCategories = categoryMap.getOrDefault(0L, new ArrayList<>());

        return rootCategories.stream()
                .map(category -> buildTree(category, categoryMap))
                .toList();
    }

    /**
     * Recursive tree builder
     */
    private CategoryTreeDTO buildTree(Category category,
                                      Map<Long, List<Category>> categoryMap) {

        CategoryTreeDTO dto = new CategoryTreeDTO();

        dto.setId(category.getId());
        dto.setName(category.getCategoryName());
        dto.setImage(category.getImage());

        List<Category> children =
                categoryMap.getOrDefault(category.getId(), new ArrayList<>());

        dto.setChildren(
                children.stream()
                        .map(child -> buildTree(child, categoryMap))
                        .toList()
        );

        return dto;
    }

    /**
     * Search categories
     */
    @Override
    public List<Category> searchCategories(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return categoryRepository
                .findByCategoryNameContainingIgnoreCase(keyword.trim())
                .stream()
                .filter(c -> c.getStatus() != null && c.getStatus() == 1)
                .toList();
    }
}