package com.ecommerce.authdemo.service.impl;

import com.ecommerce.authdemo.dto.*;
import com.ecommerce.authdemo.entity.Product;
import com.ecommerce.authdemo.entity.ProductImage;
import com.ecommerce.authdemo.entity.ProductVariant;
import com.ecommerce.authdemo.entity.ProductView;
import com.ecommerce.authdemo.specification.ProductSpecification;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import com.ecommerce.authdemo.repository.*;
import com.ecommerce.authdemo.service.ProductService;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductPincodeRepository productPincodeRepository;
    private final ProductViewRepository productViewRepository;

    public ProductServiceImpl(ProductRepository productRepository,
                              ProductImageRepository productImageRepository,
                              ProductVariantRepository variantRepository,
                              ProductPincodeRepository productPincodeRepository,
                              ProductViewRepository productViewRepository) {

            this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.variantRepository = variantRepository;
        this.productPincodeRepository = productPincodeRepository;
        this.productViewRepository = productViewRepository;

    }

    // CATEGORY PRODUCTS
    @Override
    public List<ProductListDTO> getProductsByCategory(Long categoryId) {

        List<Product> products =
                productRepository.findByCategoryIdAndStatus(categoryId, "active");

        return products.stream()
                .map(this::mapToListDTO)
                .collect(Collectors.toList());
    }

    // SUBCATEGORY PRODUCTS
    @Override
    public List<ProductListDTO> getProductsBySubCategory(Long subCategoryId) {

        List<Product> products =
                productRepository.findBySubcategoryIdAndStatus(subCategoryId, "active");

        return products.stream()
                .map(this::mapToListDTO)
                .collect(Collectors.toList());
    }

    // PRODUCT DETAILS
    @Override
    public ProductDTO getProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductDTO dto = mapToDTO(product);

        // PRODUCT IMAGES
        List<ProductImageDTO> images =
                productImageRepository.findByProductIdOrderBySortOrderAsc(id)
                        .stream()
                        .map(this::mapImageDTO)
                        .collect(Collectors.toList());

        dto.setImages(images);

        // PRODUCT VARIANTS
        List<ProductVariantDTO> variants =
                variantRepository.findByProductId(id)
                        .stream()
                        .map(this::mapVariantDTO)
                        .collect(Collectors.toList());

        dto.setVariants(variants);

        return dto;
    }

    // SEARCH PRODUCTS
    @Override
    public List<ProductListDTO> searchProducts(String keyword) {

        List<Product> products =
                productRepository.findByNameContainingIgnoreCaseAndStatus(keyword, "active");

        return products.stream()
                .map(this::mapToListDTO)
                .collect(Collectors.toList());
    }

    // ENTITY → PRODUCT DTO
    private ProductDTO mapToDTO(Product product) {

        ProductDTO dto = new ProductDTO();

        dto.setId(product.getId());
        dto.setCategoryId(product.getCategoryId());
        dto.setSubcategoryId(product.getSubcategoryId());
        dto.setSellerId(product.getSellerId());

        dto.setName(product.getName());
        dto.setSku(product.getSku());
        dto.setHsnCode(product.getHsnCode());
        dto.setProductMaterialType(product.getProductMaterialType());

        dto.setShortDescription(product.getShortDescription());
        dto.setDescription(product.getDescription());
        dto.setFeatures(product.getFeatures());
        dto.setSpecifications(product.getSpecifications());
        dto.setReturnPolicy(product.getReturnPolicy());

        dto.setGstPercentage(product.getGstPercentage());
        dto.setProductWeight(product.getProductWeight());

        dto.setDeliveryTimeMin(product.getDeliveryTimeMin());
        dto.setDeliveryTimeMax(product.getDeliveryTimeMax());

        dto.setStatus(product.getStatus());
        dto.setCreatedAt(product.getCreatedAt());

        return dto;
    }

    // ENTITY → LIST DTO
    private ProductListDTO mapToListDTO(Product product) {

        ProductListDTO dto = new ProductListDTO();

        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setSku(product.getSku());
        dto.setCategoryId(product.getCategoryId());
        dto.setSubcategoryId(product.getSubcategoryId());
        dto.setStatus(product.getStatus());

        // PRIMARY IMAGE
        ProductImage image =
                productImageRepository.findTopByProductIdAndIsPrimaryTrue(product.getId());

        if (image != null) {
            dto.setImage(image.getImagePath());
        }

        return dto;
    }

    // IMAGE ENTITY → DTO
    private ProductImageDTO mapImageDTO(ProductImage image) {

        ProductImageDTO dto = new ProductImageDTO();

        dto.setId(image.getId());
        dto.setProductId(image.getProductId());
        dto.setImagePath(image.getImagePath());
        dto.setIsPrimary(image.getIsPrimary());
        dto.setSortOrder(image.getSortOrder());

        return dto;
    }

    // VARIANT ENTITY → DTO
    private ProductVariantDTO mapVariantDTO(ProductVariant variant){

        ProductVariantDTO dto = new ProductVariantDTO();

        dto.setId(variant.getId());
        dto.setProductId(variant.getProductId());
        dto.setColor(variant.getColor());
        dto.setSize(variant.getSize());
        dto.setSku(variant.getSku());

        dto.setMrpPrice(variant.getMrpPrice());
        dto.setSellingPrice(variant.getSellingPrice());
        dto.setFinalPrice(variant.getFinalPrice());

        dto.setStock(variant.getStock());
        dto.setDiscountPercentage(variant.getDiscountPercentage());
        dto.setVideoPath(variant.getVideoPath());

        return dto;
    }

    @Override
    public DeliveryCheckDTO checkDelivery(Long productId, Long pincodeId) {

        boolean deliverable = productPincodeRepository
                .findByProductIdAndPincodeIdAndStatus(productId, pincodeId, 1)
                .isPresent();

        DeliveryCheckDTO dto = new DeliveryCheckDTO();
        dto.setProductId(productId);
        dto.setPincodeId(pincodeId);
        dto.setDeliverable(deliverable);

        return dto;
    }

    @Override
    public void saveProductView(ProductViewDTO dto) {

        ProductView view = new ProductView();

        view.setProductId(dto.getProductId());
        view.setUserId(dto.getUserId());
        view.setSessionId(dto.getSessionId());
        view.setIpAddress(dto.getIpAddress());
        view.setUserAgent(dto.getUserAgent());
        view.setViewedAt(LocalDateTime.now());   // IMPORTANT


        productViewRepository.save(view);
    }

    @Override
    public List<ProductListDTO> getRecentlyViewed(Long userId, String sessionId) {

        List<ProductView> views;

        if (userId != null) {
            views = productViewRepository.findTop10ByUserIdOrderByViewedAtDesc(userId);
        } else {
            views = productViewRepository.findTop10BySessionIdOrderByViewedAtDesc(sessionId);
        }

        List<Long> productIds = views.stream()
                .map(ProductView::getProductId)
                .distinct()
                .toList();

        return productRepository.findAllById(productIds)
                .stream()
                .map(this::mapToListDTO)
                .toList();
    }

    @Override
    public List<ProductListDTO> getMostViewedProducts() {

        List<Long> productIds = productViewRepository.findMostViewedProductIds();

        return productRepository.findAllById(productIds)
                .stream()
                .map(this::mapToListDTO)
                .toList();
    }
    @Override
    public Page<ProductListDTO> filterProducts(ProductFilterRequestDTO request) {

        // Sorting
        Sort sort = Sort.by(
                Sort.Direction.fromString(request.getSortDirection()),
                request.getSortBy()
        );

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                sort
        );

        // Specification filtering
        Specification<Product> spec =
                ProductSpecification.filterProducts(request);

        Page<Product> productPage =
                productRepository.findAll(spec, pageable);

        return productPage.map(this::mapToListDTO);
    }
}