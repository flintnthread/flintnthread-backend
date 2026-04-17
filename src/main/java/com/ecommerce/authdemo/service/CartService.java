package com.ecommerce.authdemo.service;

import com.ecommerce.authdemo.dto.AddToCartDTO;
import com.ecommerce.authdemo.dto.CartResponseDTO;
import com.ecommerce.authdemo.entity.Cart;

public interface CartService {

        CartResponseDTO addToCart(AddToCartDTO dto);
        CartResponseDTO getCart();
        CartResponseDTO updateQuantity(Long itemId, Integer quantity);
        CartResponseDTO removeItem(Long itemId);
        CartResponseDTO applyCoupon(String code);
        Integer getCartCount();
        void clearCart();
}