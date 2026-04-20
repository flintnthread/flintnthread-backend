package com.ecommerce.authdemo.service;

import com.ecommerce.authdemo.dto.ProductDTO;
import com.ecommerce.authdemo.dto.ProductViewDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    ProductDTO createProduct(ProductDTO dto);

    ProductDTO getProduct(Long id);

    Page<ProductDTO> getAllProducts(Pageable pageable);

    Page<ProductDTO> getByCategory(Long categoryId, Pageable pageable);

    List<ProductDTO> getRecentProducts();

    List<ProductDTO> getPopularProducts();

    List<ProductDTO> getTrendingProducts();

    List<ProductDTO> getRelatedProducts(Long productId);

    List<ProductDTO> searchProducts(String keyword);

    List<ProductDTO> getSellerProducts(Long sellerId);

    void trackView(ProductViewDTO dto);

    List<ProductDTO> getBySubCategory(Long subCategoryId);




    List<ProductDTO> getTopSellingPriceProducts();

    List<ProductDTO> getTopProductsByCategory(Long categoryId);

    List<ProductDTO> getTopDiscountProducts();

    List<ProductDTO> getByMainCategory(Long mainCategoryId);

}