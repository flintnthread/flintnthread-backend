package com.ecommerce.authdemo.service.impl;

import com.ecommerce.authdemo.dto.ProductDTO;
import com.ecommerce.authdemo.dto.ProductImageDTO;
import com.ecommerce.authdemo.dto.WishlistResponse;
import com.ecommerce.authdemo.entity.Product;
import com.ecommerce.authdemo.entity.ProductImage;
import com.ecommerce.authdemo.entity.ProductVariant;
import com.ecommerce.authdemo.entity.User;
import com.ecommerce.authdemo.entity.Wishlist;
import com.ecommerce.authdemo.mapper.ProductMapper;
import com.ecommerce.authdemo.repository.WishlistRepository;
import com.ecommerce.authdemo.service.ProductService;
import com.ecommerce.authdemo.service.WishlistService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductService productService;
    private final ProductMapper productMapper;


    public WishlistServiceImpl(WishlistRepository wishlistRepository,
                               ProductService productService,
                               ProductMapper productMapper) {
        this.wishlistRepository = wishlistRepository;
        this.productService = productService;
        this.productMapper = productMapper;
    }

    @Override
    public WishlistResponse addToWishlist(Long userId, Long productId) {

        // Check if product exists before adding to wishlist
        ProductDTO productDTO = productService.getProduct(productId);
        if (productDTO == null) {
            throw new RuntimeException("Product not found with ID: " + productId);
        }

        // Convert ProductDTO to Product entity for wishlist
        Product product = new Product();
        product.setId(productDTO.getId());
        product.setName(productDTO.getName());
        // Set other necessary fields if needed
        product.setCategoryId(productDTO.getCategoryId() != null ? productDTO.getCategoryId().intValue() : null);
        product.setSubcategoryId(productDTO.getSubcategoryId() != null ? productDTO.getSubcategoryId().intValue() : null);
        product.setStatus(productDTO.getStatus());
        product.setSellerId(productDTO.getSellerId());

        Wishlist wishlist = wishlistRepository
                .findByUserIdAndProductId(userId, productId)
                .orElseGet(() -> {

                    Wishlist w = new Wishlist();

                    // SET USER
                    User user = new User();
                    user.setId(userId);
                    w.setUser(user);

                    // SET PRODUCT (using actual product found)
                    w.setProduct(product);

                    return wishlistRepository.save(w);
                });

        return buildResponse(wishlist);
    }
    @Override
    public List<WishlistResponse> getUserWishlist(Long userId) {

        List<Wishlist> wishlistItems =
                wishlistRepository.findWishlistWithProduct(userId);

        return wishlistItems.stream()
                .map(this::buildResponse)
                .toList();
    }

    @Override
    @Transactional
    public void removeFromWishlist(Long userId, Long productId) {

        Wishlist wishlist = wishlistRepository
                .findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new RuntimeException("Wishlist item not found"));

        wishlistRepository.delete(wishlist);
    }

    @Override
    public boolean isProductInWishlist(Long userId, Long productId) {
        return wishlistRepository
                .findByUserIdAndProductId(userId, productId)
                .isPresent();
    }


    private WishlistResponse buildResponse(Wishlist wishlist) {

        Product product = wishlist.getProduct();

        WishlistResponse response = new WishlistResponse();

        response.setWishlistId(wishlist.getId());
        response.setWishlistId(wishlist.getId());
        response.setProductId(product.getId());
        response.setProductName(product.getName());
        response.setAddedAt(wishlist.getCreatedAt());

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            // Convert ProductImage entities to ProductImageDTOs (same as ProductDTO)
            List<ProductImageDTO> imageDTOs = product.getImages().stream()
                    .map(productMapper::toImageDTO)
                    .collect(Collectors.toList());
            
            response.setImages(imageDTOs);
            
            // Get the primary image or first image for backward compatibility
            ProductImage primaryImage = product.getImages().stream()
                    .filter(img -> img.getIsPrimary() != null && img.getIsPrimary())
                    .findFirst()
                    .orElse(product.getImages().iterator().next());
            
            // Use imageUrl (absolute URL) like products do
            String primaryImageUrl = null;
            if (primaryImage.getImagePath() != null) {
                // ProductImage entity has imagePath, let ProductMapper convert to URL
                primaryImageUrl = productMapper.resolveImageUrl(primaryImage.getImagePath());
            }
            
            // Set both fields for backward compatibility
            response.setImage(primaryImageUrl);
            response.setImageUrl(primaryImageUrl);
        }

        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            ProductVariant variant = product.getVariants().iterator().next();

            response.setMrpPrice(variant.getSellingPrice());
            response.setSellingPrice(variant.getSellingPrice());
            response.setSize(variant.getSize());
            response.setColor(variant.getColor());
            response.setInStock(variant.getStock() > 0);
        }

        return response;
    }
}