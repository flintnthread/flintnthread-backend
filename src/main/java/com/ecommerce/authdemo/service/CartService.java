package com.ecommerce.authdemo.service;


import com.ecommerce.authdemo.dto.AddToCartRequest;
import com.ecommerce.authdemo.dto.UpdateCartItemRequest;
import com.ecommerce.authdemo.entity.Cart;

public interface CartService {

        Cart getCart(Long userId);

        Cart addToCart(Long userId, AddToCartRequest request);

        Cart updateCartItem(Long userId, UpdateCartItemRequest request);

        void removeCartItem(Long userId, Long cartItemId);

        void clearCart(Long userId);
    }

