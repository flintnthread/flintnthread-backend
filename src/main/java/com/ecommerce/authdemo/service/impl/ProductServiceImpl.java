package com.ecommerce.authdemo.service.impl;

import com.ecommerce.authdemo.dto.*;
import com.ecommerce.authdemo.entity.Product;
import com.ecommerce.authdemo.entity.ProductView;
import com.ecommerce.authdemo.entity.User;
import com.ecommerce.authdemo.mapper.ProductMapper;
import com.ecommerce.authdemo.util.SizeColorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import com.ecommerce.authdemo.repository.*;
import com.ecommerce.authdemo.service.ProductService;
import com.ecommerce.authdemo.specification.ProductSpecification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepo;
    private final ProductVariantRepository variantRepo;
    private final ProductViewRepository viewRepo;
    private final SizeColorMapper sizeColorMapper;

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
    public List<ProductDTO> getRecentProductsByMainCategory(Long mainCategoryId) {
        return productRepo.findRecentProductsByMainCategory(mainCategoryId)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public List<ProductDTO> getLatestProductsByMainCategory(Long mainCategoryId) {
        return productRepo.findLatestProductsByMainCategory(mainCategoryId)
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
    public List<ProductDTO> getPopularProductsByMainCategory(Long mainCategoryId) {
        return productRepo.findPopularProductsByMainCategory(mainCategoryId)
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
    public List<ProductDTO> getTrendingProductsByMainCategory(Long mainCategoryId) {
        LocalDateTime last7Days = LocalDateTime.now().minusDays(7);

        return productRepo.findTrendingProductsByMainCategory(last7Days, mainCategoryId)
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
    public List<ProductDTO> getRelatedProductsByMainCategory(Long mainCategoryId) {
        return productRepo.findRelatedProductsByMainCategory(mainCategoryId)
                .stream()
                .map(mapper::toDTO)
                .toList();
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

    @Override
    public List<ProductDTO> getDiscountProductsByMainCategoryAsc(Long mainCategoryId) {
        return productRepo.findDiscountProductsByMainCategoryAsc(mainCategoryId)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public List<ProductDTO> getProductsByMainCategoryAndExactDiscountPercentage(Long mainCategoryId, Double discountPercentage) {
        return productRepo.findProductsByMainCategoryAndExactDiscountPercentage(mainCategoryId, discountPercentage)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public List<ProductDTO> getProductsByMainCategoryWithDiscountLessThanEqual(Long mainCategoryId, Double maxDiscountPercentage) {
        return productRepo.findProductsByMainCategoryWithDiscountLessThanEqual(mainCategoryId, maxDiscountPercentage)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public List<ProductDTO> getSpotlightProductsByMainCategory(Long mainCategoryId) {
        return productRepo.findSpotlightProductsByMainCategory(mainCategoryId)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public List<ProductDTO> getUniqueProductsByMainCategory(Long mainCategoryId) {
        return productRepo.findUniqueProductsByMainCategory(mainCategoryId)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public List<ProductDTO> getTopCollectionsByMainCategory(Long mainCategoryId) {
        return productRepo.findTopCollectionsByMainCategory(mainCategoryId)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public List<ProductDTO> getRecommendedProductsByMainCategory(Long mainCategoryId, Long userId, String sessionId) {
        return productRepo.findRecommendedProductsByMainCategory(mainCategoryId, userId, sessionId)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public List<ProductDTO> getRecentlyViewedProductsByMainCategory(Long mainCategoryId, Long userId, String sessionId) {
        List<Long> productIds = productRepo.findRecentlyViewedProductIdsByMainCategory(mainCategoryId, userId, sessionId);
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        return mapProductsByIdOrder(productIds);
    }

    @Override
    public List<ProductDTO> getByMainCategory(Long mainCategoryId) {
        return productRepo.findByMainCategoryFull(mainCategoryId)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public Page<ProductDTO> getFilteredProducts(ProductFilterRequestDTO filterRequest) {
        Specification<Product> spec = ProductSpecification.filterProducts(filterRequest);
        
        Pageable pageable = PageRequest.of(
            filterRequest.getPage(),
            filterRequest.getSize(),
            Sort.by(
                Sort.Direction.fromString(filterRequest.getSortDirection()),
                filterRequest.getSortBy()
            )
        );
        
        return productRepo.findAll(spec, pageable)
                .map(mapper::toDTO);
    }

    @Override
    public List<String> getAllSizes() {
        List<String> sizeIdsOrNames = variantRepo.findAllDistinctSizes();
        return sizeIdsOrNames.stream()
                .map(sizeColorMapper::getSizeName)
                .distinct()
                .toList();
    }

    @Override
    public List<String> getAllColors() {
        List<String> colorIdsOrNames = variantRepo.findAllDistinctColors();
        return colorIdsOrNames.stream()
                .map(sizeColorMapper::getColorName)
                .distinct()
                .toList();
    }

    @Override
    public List<String> getSizesByProductId(Long productId) {
        List<String> sizeIdsOrNames = variantRepo.findDistinctSizesByProductId(productId);
        return sizeIdsOrNames.stream()
                .map(sizeColorMapper::getSizeName)
                .distinct()
                .toList();
    }

    @Override
    public List<String> getColorsByProductId(Long productId) {
        List<String> colorIdsOrNames = variantRepo.findDistinctColorsByProductId(productId);
        return colorIdsOrNames.stream()
                .map(sizeColorMapper::getColorName)
                .distinct()
                .toList();
    }

    private List<ProductDTO> mapProductsByIdOrder(List<Long> productIds) {
        List<Product> products = productRepo.findAllById(productIds);
        Map<Long, ProductDTO> dtoById = new LinkedHashMap<>();
        products.forEach(product -> dtoById.put(product.getId(), mapper.toDTO(product)));
        return productIds.stream()
                .map(dtoById::get)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

}