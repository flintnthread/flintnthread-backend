package com.ecommerce.authdemo.service.impl;



import com.ecommerce.authdemo.dto.AddToCartRequest;
import com.ecommerce.authdemo.dto.UpdateCartItemRequest;
import com.ecommerce.authdemo.entity.Cart;
import com.ecommerce.authdemo.entity.CartItem;
import com.ecommerce.authdemo.repository.CartItemRepository;
import com.ecommerce.authdemo.repository.CartRepository;
import com.ecommerce.authdemo.service.CartService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

    @Service
    @RequiredArgsConstructor
    @Transactional
    public class CartServiceImpl implements CartService {

        private final CartRepository cartRepository;
        private final CartItemRepository cartItemRepository;

        @Override
        public Cart getCart(Long userId) {

            return cartRepository.findByUserId(userId)
                    .orElseGet(() -> createCart(userId));
        }

        private Cart createCart(Long userId) {

            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setItems(new ArrayList<>());
            cart.setTotalAmount(BigDecimal.ZERO);
            cart.setFinalAmount(BigDecimal.ZERO);

            return cartRepository.save(cart);
        }

        @Override
        public Cart addToCart(Long userId, AddToCartRequest request) {

            Cart cart = getCart(userId);

            Optional<CartItem> existingItem =
                    cartItemRepository.findByCartIdAndProductId(cart.getId(), request.getProductId());

            if (existingItem.isPresent()) {

                CartItem item = existingItem.get();
                item.setQuantity(item.getQuantity() + request.getQuantity());

                cartItemRepository.save(item);

            } else {

                CartItem item = new CartItem();

                item.setCart(cart);
                item.setProductId(request.getProductId());
                item.setSellerId(request.getSellerId());
                item.setQuantity(request.getQuantity());

                // Normally price comes from product service
                item.setUnitPrice(BigDecimal.valueOf(500));

                item.setTotalPrice(item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())));

                cartItemRepository.save(item);
            }

            return recalculateCart(cart);
        }

        @Override
        public Cart updateCartItem(Long userId, UpdateCartItemRequest request) {

            Cart cart = getCart(userId);

            CartItem item = cartItemRepository.findById(request.getCartItemId())
                    .orElseThrow(() -> new RuntimeException("Cart item not found"));

            item.setQuantity(request.getQuantity());

            item.setTotalPrice(item.getUnitPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity())));

            cartItemRepository.save(item);

            return recalculateCart(cart);
        }

        @Override
        public void removeCartItem(Long userId, Long cartItemId) {

            Cart cart = getCart(userId);

            cartItemRepository.deleteById(cartItemId);

            recalculateCart(cart);
        }

        @Override
        public void clearCart(Long userId) {

            Cart cart = getCart(userId);

            cartItemRepository.deleteByCartId(cart.getId());

            cart.setTotalAmount(BigDecimal.ZERO);
            cart.setFinalAmount(BigDecimal.ZERO);

            cartRepository.save(cart);
        }

        private Cart recalculateCart(Cart cart) {

            var items = cartItemRepository.findByCartId(cart.getId());

            BigDecimal total = BigDecimal.ZERO;

            for (CartItem item : items) {

                total = total.add(item.getTotalPrice());
            }

            cart.setTotalAmount(total);
            cart.setFinalAmount(total);

            return cartRepository.save(cart);
        }
    }

