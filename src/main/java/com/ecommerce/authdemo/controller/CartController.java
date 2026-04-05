package com.ecommerce.authdemo.controller;


import com.ecommerce.authdemo.dto.AddToCartRequest;
import com.ecommerce.authdemo.dto.UpdateCartItemRequest;
import com.ecommerce.authdemo.entity.Cart;
import com.ecommerce.authdemo.service.CartService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

    @RestController
    @RequestMapping("/api/cart")
    @RequiredArgsConstructor
    public class CartController {

        private final CartService cartService;

        // Get user cart
        @GetMapping("/{userId}")
        public Cart getCart(@PathVariable Long userId) {
            return cartService.getCart(userId);
        }

        // Add product to cart
        @PostMapping("/{userId}/add")
        public Cart addToCart(
                @PathVariable Long userId,
                @RequestBody AddToCartRequest request) {

            return cartService.addToCart(userId, request);
        }

        // Update cart item
        @PutMapping("/{userId}/update")
        public Cart updateCartItem(
                @PathVariable Long userId,
                @RequestBody UpdateCartItemRequest request) {

            return cartService.updateCartItem(userId, request);
        }

        // Remove cart item
        @DeleteMapping("/{userId}/remove/{cartItemId}")
        public void removeCartItem(
                @PathVariable Long userId,
                @PathVariable Long cartItemId) {

            cartService.removeCartItem(userId, cartItemId);
        }

        // Clear cart
        @DeleteMapping("/{userId}/clear")
        public void clearCart(@PathVariable Long userId) {
            cartService.clearCart(userId);
        }
    }

