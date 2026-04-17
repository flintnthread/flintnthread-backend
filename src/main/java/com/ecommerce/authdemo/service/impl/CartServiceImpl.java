package com.ecommerce.authdemo.service.impl;

import com.ecommerce.authdemo.dto.*;
import com.ecommerce.authdemo.entity.Cart;
import com.ecommerce.authdemo.entity.CartItem;
import com.ecommerce.authdemo.repository.CartItemRepository;
import com.ecommerce.authdemo.repository.CartRepository;
import com.ecommerce.authdemo.service.CartService;
import com.ecommerce.authdemo.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final SecurityUtil securityUtil;

    // =============================
    // 🔥 ADD TO CART
    // =============================
    @Override
    @Transactional
    public CartResponseDTO addToCart(AddToCartDTO dto) {

        Long userId = securityUtil.getCurrentUser().getId();

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createCart(userId));

        Optional<CartItem> existingItem =
                cartItemRepository.findByCartIdAndProductIdAndVariantId(
                        cart.getId(), dto.getProductId(), dto.getVariantId()
                );

        if (existingItem.isPresent()) {

            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + dto.getQuantity());
            item.setTotalPrice(item.getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity())));

            cartItemRepository.save(item);

        } else {

            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProductId(dto.getProductId());
            item.setVariantId(dto.getVariantId());
            item.setQuantity(dto.getQuantity());

            // 🔥 TEMP PRICE (replace with product service)
            BigDecimal price = BigDecimal.valueOf(399);

            item.setPrice(price);
            item.setTotalPrice(price.multiply(BigDecimal.valueOf(dto.getQuantity())));

            cartItemRepository.save(item);
        }

        return buildCartResponse(cart);
    }

    // =============================
    // 🔥 GET CART (UPDATED)
    // =============================
    @Override
    public CartResponseDTO getCart() {

        Long userId = securityUtil.getCurrentUser().getId();

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        return buildCartResponse(cart);
    }

    // =============================
    // 🔥 UPDATE QTY
    // =============================
    @Override
    public CartResponseDTO updateQuantity(Long itemId, Integer quantity) {

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        item.setQuantity(quantity);
        item.setTotalPrice(item.getPrice()
                .multiply(BigDecimal.valueOf(quantity)));

        cartItemRepository.save(item);

        return buildCartResponse(item.getCart());
    }

    // =============================
    // 🔥 REMOVE ITEM
    // =============================
    @Override
    public CartResponseDTO removeItem(Long itemId) {

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        Cart cart = item.getCart();

        cartItemRepository.delete(item);

        return buildCartResponse(cart);
    }

    // =============================
    // 🔥 CLEAR CART
    // =============================
    @Override
    public void clearCart() {

        Cart cart = getCartEntity();

        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

        cartItemRepository.deleteAll(items);

        cart.setTotalAmount(BigDecimal.ZERO);
        cart.setFinalAmount(BigDecimal.ZERO);

        cartRepository.save(cart);
    }

    // =============================
    // 🔥 APPLY COUPON
    // =============================
    @Override
    public CartResponseDTO applyCoupon(String code) {

        if (!code.equalsIgnoreCase("SAVE500")) {
            throw new RuntimeException("Invalid coupon");
        }

        Cart cart = getCartEntity();

        return buildCartResponse(cart, BigDecimal.valueOf(500));
    }

    // =============================
    // 🔥 CART COUNT
    // =============================
    @Override
    public Integer getCartCount() {

        Cart cart = getCartEntity();

        return cartItemRepository.findByCartId(cart.getId()).size();
    }

    // =============================
    // 🔥 BUILD RESPONSE (MAIN LOGIC)
    // =============================
    private CartResponseDTO buildCartResponse(Cart cart) {
        return buildCartResponse(cart, BigDecimal.ZERO);
    }

    private CartResponseDTO buildCartResponse(Cart cart, BigDecimal discount) {

        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

        List<CartItemResponseDTO> itemDTOs = items.stream().map(item -> {

            CartItemResponseDTO dto = new CartItemResponseDTO();

            dto.setItemId(item.getId());
            dto.setProductId(item.getProductId());

            // 🔥 Replace with product table later
            dto.setName("Product " + item.getProductId());
            dto.setImage("https://via.placeholder.com/150");

            dto.setPrice(item.getPrice());
            dto.setOriginalPrice(item.getPrice().add(BigDecimal.valueOf(300)));

            dto.setQuantity(item.getQuantity());
            dto.setTotal(item.getTotalPrice());

            return dto;

        }).toList();

        // 🔥 CALCULATIONS
        BigDecimal subtotal = itemDTOs.stream()
                .map(CartItemResponseDTO::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal delivery = BigDecimal.valueOf(99);

        BigDecimal finalTotal = subtotal.subtract(discount).add(delivery);

        PriceSummaryDTO summary = new PriceSummaryDTO();
        summary.setSubtotal(subtotal);
        summary.setDiscount(discount);
        summary.setDeliveryCharge(delivery);
        summary.setFinalTotal(finalTotal);

        CartResponseDTO response = new CartResponseDTO();
        response.setItems(itemDTOs);
        response.setPriceSummary(summary);
        response.setCouponApplied(discount.compareTo(BigDecimal.ZERO) > 0 ? "SAVE500" : null);

        return response;
    }

    // =============================
    // 🔥 HELPERS
    // =============================
    private Cart createCart(Long userId) {

        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setTotalAmount(BigDecimal.ZERO);
        cart.setFinalAmount(BigDecimal.ZERO);

        return cartRepository.save(cart);
    }

    private Cart getCartEntity() {

        Long userId = securityUtil.getCurrentUser().getId();

        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
    }
}