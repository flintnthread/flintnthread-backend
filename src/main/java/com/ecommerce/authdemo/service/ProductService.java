package com.ecommerce.authdemo.service;

import com.ecommerce.authdemo.dto.ProductDTO;
import com.ecommerce.authdemo.dto.ProductFilterRequestDTO;
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
    List<ProductDTO> getRecentProductsByMainCategory(Long mainCategoryId);

    List<ProductDTO> getPopularProducts();
    List<ProductDTO> getPopularProductsByMainCategory(Long mainCategoryId);

    List<ProductDTO> getTrendingProducts();
    List<ProductDTO> getTrendingProductsByMainCategory(Long mainCategoryId);

    List<ProductDTO> getRelatedProducts(Long productId);
    List<ProductDTO> getRelatedProductsByMainCategory(Long mainCategoryId);

    List<ProductDTO> searchProducts(String keyword);

    List<ProductDTO> getSellerProducts(Long sellerId);

    void trackView(ProductViewDTO dto);

    List<ProductDTO> getBySubCategory(Long subCategoryId);




    List<ProductDTO> getTopSellingPriceProducts();

    List<ProductDTO> getTopProductsByCategory(Long categoryId);

    List<ProductDTO> getTopDiscountProducts();
    List<ProductDTO> getDiscountProductsByMainCategoryAsc(Long mainCategoryId);

    List<ProductDTO> getByMainCategory(Long mainCategoryId);

    Page<ProductDTO> getFilteredProducts(ProductFilterRequestDTO filterRequest);

    List<String> getAllSizes();

    List<String> getAllColors();

    List<String> getSizesByProductId(Long productId);

    List<String> getColorsByProductId(Long productId);

}