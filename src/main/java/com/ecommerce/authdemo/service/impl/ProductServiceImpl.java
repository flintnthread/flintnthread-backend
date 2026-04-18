package com.ecommerce.authdemo.service.impl;

import com.ecommerce.authdemo.dto.*;
import com.ecommerce.authdemo.entity.Product;
import com.ecommerce.authdemo.entity.ProductView;
import com.ecommerce.authdemo.entity.User;
import com.ecommerce.authdemo.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import com.ecommerce.authdemo.repository.*;
import com.ecommerce.authdemo.service.ProductService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepo;
    private final ProductVariantRepository variantRepo;
    private final ProductViewRepository viewRepo;

    private final ProductMapper mapper;

    @Override
    @Transactional(readOnly = false)
    public ProductDTO createProduct(ProductDTO dto) {
        Product product = mapper.toEntity(dto);
        return mapper.toDTO(productRepo.save(product));
    }

    @Override
    public ProductDTO getProduct(Long id) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        return mapper.toDTO(product);
    }

    @Override
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        return productRepo.findAll(pageable)
                .map(mapper::toDTO);
    }

    @Override
    public Page<ProductDTO> getByCategory(Long categoryId, Pageable pageable) {
        return productRepo.findByCategoryId(categoryId, pageable)
                .map(mapper::toDTO);
    }

    @Override
    public List<ProductDTO> getRecentProducts() {
        return productRepo.findTop10ByOrderByCreatedAtDesc()
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public List<ProductDTO> getPopularProducts() {
        return productRepo.findPopularProducts()
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public List<ProductDTO> getTrendingProducts() {

        LocalDateTime last7Days = LocalDateTime.now().minusDays(7);

        return productRepo.findTrendingProducts(last7Days)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public List<ProductDTO> getRelatedProducts(Long productId) {
        Product product = productRepo.findById(productId).orElseThrow();

        return productRepo.findTop10ByCategoryIdAndIdNot(
                Long.valueOf(product.getCategoryId()), productId
        ).stream().map(mapper::toDTO).toList();
    }

    @Override
    public List<ProductDTO> searchProducts(String keyword) {
        return productRepo.search(keyword)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public List<ProductDTO> getSellerProducts(Long sellerId) {
        return productRepo.findBySellerId(sellerId)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = false)
    public void trackView(ProductViewDTO dto) {

        ProductView view = new ProductView();

        Product product = new Product();
        product.setId(dto.getProductId());

        view.setProduct(product);
        view.setSessionId(dto.getSessionId());

        if (dto.getUserId() != null) {
            User user = new User();
            user.setId(dto.getUserId());
            view.setUser(user);
        }

        view.setSessionId(dto.getSessionId());

        viewRepo.save(view);
    }


    @Override
    public List<ProductDTO> getBySubCategory(Long subCategoryId) {

        List<Product> products = productRepo.findBySubCategoryFull(subCategoryId);

        return products.stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public List<ProductDTO> getTopSellingPriceProducts() {
        return productRepo.findTopSellingProducts()
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public List<ProductDTO> getTopProductsByCategory(Long categoryId) {
        return productRepo.findTopProductsByCategory(categoryId)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }


    @Override
    public List<ProductDTO> getTopDiscountProducts() {

        return productRepo.findTopDiscountProducts()
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

}