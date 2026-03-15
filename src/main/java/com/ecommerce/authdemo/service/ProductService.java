package com.ecommerce.authdemo.service;

import com.ecommerce.authdemo.dto.DeliveryCheckDTO;
import com.ecommerce.authdemo.dto.ProductDTO;
import com.ecommerce.authdemo.dto.ProductFilterRequestDTO;
import com.ecommerce.authdemo.dto.ProductListDTO;
import com.ecommerce.authdemo.dto.ProductViewDTO;

import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {

    // CATEGORY PRODUCTS
    List<ProductListDTO> getProductsByCategory(Long categoryId);

    // SUBCATEGORY PRODUCTS
    List<ProductListDTO> getProductsBySubCategory(Long subCategoryId);

    // PRODUCT DETAILS
    ProductDTO getProduct(Long id);

    // SEARCH PRODUCTS
    List<ProductListDTO> searchProducts(String keyword);

    // DELIVERY CHECK
    DeliveryCheckDTO checkDelivery(Long productId, Long pincodeId);

    // SAVE PRODUCT VIEW
    void saveProductView(ProductViewDTO dto);

    // RECENTLY VIEWED
    List<ProductListDTO> getRecentlyViewed(Long userId, String sessionId);

    // MOST VIEWED PRODUCTS
    List<ProductListDTO> getMostViewedProducts();

    // 🔥 ADVANCED FILTER API
    Page<ProductListDTO> filterProducts(ProductFilterRequestDTO request);

}