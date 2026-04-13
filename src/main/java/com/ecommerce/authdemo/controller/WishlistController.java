package com.ecommerce.authdemo.controller;

import com.ecommerce.authdemo.dto.ProductDTO;
import com.ecommerce.authdemo.dto.WishlistProductResponse;
import com.ecommerce.authdemo.dto.WishlistRequest;
import com.ecommerce.authdemo.dto.WishlistResponse;
import com.ecommerce.authdemo.service.WishlistService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@CrossOrigin("*")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService){
        this.wishlistService = wishlistService;
    }

    // -------------------------
    // ADD TO WISHLIST (FULL PRODUCT RESPONSE)
    // -------------------------
    @PostMapping("/add")
    public WishlistResponse addToWishlist(@RequestBody WishlistRequest request){
        return wishlistService.addToWishlist(
                request.getUserId(),
                request.getProductId()
        );
    }

    // -------------------------
    // GET USER WISHLIST
    // -------------------------
    @GetMapping("/user/{userId}")
    public List<WishlistResponse> getWishlist(@PathVariable Long userId){
        return wishlistService.getUserWishlist(userId);
    }

    // -------------------------
    // REMOVE FROM WISHLIST
    // -------------------------
    @DeleteMapping("/remove")
    public String removeFromWishlist(@RequestParam Long userId,
                                     @RequestParam Long productId){

        wishlistService.removeFromWishlist(userId, productId);
        return "Product removed from wishlist";
    }

    // -------------------------
    // CHECK PRODUCT IN WISHLIST
    // -------------------------
    @GetMapping("/check")
    public boolean checkWishlist(@RequestParam Long userId,
                                 @RequestParam Long productId){

        return wishlistService.isProductInWishlist(userId, productId);
    }
}