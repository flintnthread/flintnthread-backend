package com.ecommerce.authdemo.controller;

import com.ecommerce.authdemo.dto.*;
import com.ecommerce.authdemo.service.ProductService;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // CATEGORY PRODUCTS
    @GetMapping("/category/{id}")
    public List<ProductListDTO> getProductsByCategory(@PathVariable Long id) {
        return productService.getProductsByCategory(id);
    }

    // SUBCATEGORY PRODUCTS
    @GetMapping("/subcategory/{id}")
    public List<ProductListDTO> getProductsBySubCategory(@PathVariable Long id) {
        return productService.getProductsBySubCategory(id);
    }

    // PRODUCT DETAILS
    @GetMapping("/{id}")
    public ProductDTO getProduct(@PathVariable Long id) {
        return productService.getProduct(id);
    }

    // SEARCH PRODUCTS
    @GetMapping("/search")
    public List<ProductListDTO> searchProducts(@RequestParam String keyword) {
        return productService.searchProducts(keyword);
    }

    // DELIVERY CHECK
    @GetMapping("/delivery-check")
    public DeliveryCheckDTO checkDelivery(
            @RequestParam Long productId,
            @RequestParam Long pincodeId) {

        return productService.checkDelivery(productId, pincodeId);
    }

    // SAVE PRODUCT VIEW
    @PostMapping("/view")
    public void saveProductView(@RequestBody ProductViewDTO dto) {
        productService.saveProductView(dto);
    }

    // RECENTLY VIEWED PRODUCTS
    @GetMapping("/recently-viewed")
    public List<ProductListDTO> getRecentlyViewed(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String sessionId) {

        return productService.getRecentlyViewed(userId, sessionId);
    }

    // MOST VIEWED PRODUCTS
    @GetMapping("/most-viewed")
    public List<ProductListDTO> getMostViewedProducts() {
        return productService.getMostViewedProducts();
    }

    // 🔥 ADVANCED PRODUCT FILTER
    @PostMapping("/filter")
    public Page<ProductListDTO> filterProducts(
            @RequestBody ProductFilterRequestDTO request) {

        return productService.filterProducts(request);
    }

}