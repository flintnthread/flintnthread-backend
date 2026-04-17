package com.ecommerce.authdemo.service.impl;

import com.ecommerce.authdemo.dto.WishlistResponse;
import com.ecommerce.authdemo.entity.Product;
import com.ecommerce.authdemo.entity.ProductVariant;
import com.ecommerce.authdemo.entity.User;
import com.ecommerce.authdemo.entity.Wishlist;
import com.ecommerce.authdemo.repository.WishlistRepository;
import com.ecommerce.authdemo.service.ProductService;
import com.ecommerce.authdemo.service.WishlistService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductService productService;


    public WishlistServiceImpl(WishlistRepository wishlistRepository,
                               ProductService productService) {
        this.wishlistRepository = wishlistRepository;
        this.productService = productService;

    }

    @Override
    public WishlistResponse addToWishlist(Long userId, Long productId) {

        Wishlist wishlist = wishlistRepository
                .findByUserIdAndProductId(userId, productId)
                .orElseGet(() -> {

                    Wishlist w = new Wishlist();

                    // SET USER
                    User user = new User();
                    user.setId(userId);
                    w.setUser(user);

                    // SET PRODUCT
                    Product product = new Product();
                    product.setId(productId);
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
            response.setImage(product.getImages().get(0).getImagePath());
        }

        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            ProductVariant variant = product.getVariants().get(0);

            response.setMrpPrice(variant.getSellingPrice());
            response.setSellingPrice(variant.getSellingPrice());
            response.setSize(variant.getSize());
            response.setColor(variant.getColor());
            response.setInStock(variant.getStock() > 0);
        }

        return response;
    }
}