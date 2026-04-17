package com.ecommerce.authdemo.controller;

import com.ecommerce.authdemo.dto.*;
import com.ecommerce.authdemo.service.ProductService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ProductDTO create(@RequestBody ProductDTO dto) {
        return productService.createProduct(dto);
    }

    @GetMapping("/{id}")
    public ProductDTO get(@PathVariable Long id) {
        return productService.getProduct(id);
    }

    @GetMapping
    public Page<ProductDTO> all(Pageable pageable) {
        return productService.getAllProducts(pageable);
    }

    @GetMapping("/category/{id}")
    public Page<ProductDTO> byCategory(@PathVariable Long id, Pageable pageable) {
        return productService.getByCategory(id, pageable);
    }

    @GetMapping("/recent")
    public List<ProductDTO> recent() {
        return productService.getRecentProducts();
    }

    @GetMapping("/popular")
    public List<ProductDTO> popular() {
        return productService.getPopularProducts();
    }

    @GetMapping("/trending")
    public List<ProductDTO> trending() {
        return productService.getTrendingProducts();
    }

    @GetMapping("/related/{id}")
    public List<ProductDTO> related(@PathVariable Long id) {
        return productService.getRelatedProducts(id);
    }

    @GetMapping("/search")
    public List<ProductDTO> search(@RequestParam String q) {
        return productService.searchProducts(q);
    }

    @PostMapping("/view")
    public void trackView(@RequestBody ProductViewDTO dto) {
        productService.trackView(dto);
    }


    @GetMapping("/subcategory/{id}")
    public List<ProductDTO> bySubCategory(@PathVariable Long id) {
        return productService.getBySubCategory(id);
    }
}