package com.ecommerce.authdemo.controller;

import com.ecommerce.authdemo.entity.Category;
import com.ecommerce.authdemo.dto.CategoryTreeDTO;
import com.ecommerce.authdemo.service.CategoryService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * 1️⃣ Get all MAIN categories
     */
    @GetMapping("/main")
    public ResponseEntity<List<Category>> getMainCategories() {

        List<Category> categories = categoryService.getMainCategories();

        return ResponseEntity.ok(categories);
    }


    /**
     * 2️⃣ Get subcategories by parentId
     */
    @GetMapping("/{parentId}/subcategories")
    public ResponseEntity<List<Category>> getSubCategories(
            @PathVariable Long parentId) {

        List<Category> categories =
                categoryService.getSubCategories(parentId);

        return ResponseEntity.ok(categories);
    }


    /**
     * 3️⃣ Get category by ID
     */
    @GetMapping("/id/{id}")
    public ResponseEntity<Category> getCategoryById(
            @PathVariable Long id) {

        Category category = categoryService.getCategory(id);

        return ResponseEntity.ok(category);
    }


    /**
     * 4️⃣ Get FULL CATEGORY TREE
     * Mobile apps use this to build nested category UI
     */
    @GetMapping("/tree")
    public ResponseEntity<List<CategoryTreeDTO>> getCategoryTree() {

        List<CategoryTreeDTO> tree =
                categoryService.getCategoryTree();

        return ResponseEntity.ok(tree);
    }


    /**
     * 5️⃣ Search categories
     */
    @GetMapping("/search")
    public ResponseEntity<List<Category>> searchCategories(
            @RequestParam String keyword) {

        List<Category> categories =
                categoryService.searchCategories(keyword);

        return ResponseEntity.ok(categories);
    }
}