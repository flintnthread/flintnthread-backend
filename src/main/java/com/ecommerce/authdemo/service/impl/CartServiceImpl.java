package com.ecommerce.authdemo.service.impl;

import com.ecommerce.authdemo.dto.*;
import com.ecommerce.authdemo.entity.Cart;
import com.ecommerce.authdemo.entity.User;
import com.ecommerce.authdemo.exception.ResourceNotFoundException;
import com.ecommerce.authdemo.exception.CartException;
import com.ecommerce.authdemo.repository.CartRepository;
import com.ecommerce.authdemo.repository.UserRepository;
import com.ecommerce.authdemo.repository.ProductImageRepository;
import com.ecommerce.authdemo.service.CartService;
import com.ecommerce.authdemo.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final SecurityUtil securityUtil;
    private final UserRepository userRepository;
    private final ProductImageRepository productImageRepository;
    
    @Value("${app.media.public-base-url}")
    private String publicBaseUrl;

    @Override
    @Transactional
    public CartResponseDTO addToCart(AddToCartDTO dto) {
        log.info("Adding item to cart: productId={}, variantId={}, quantity={}", 
                dto.getProductId(), dto.getVariantId(), dto.getQuantity());
        
        validateAddToCartRequest(dto);
        
        Long userId = securityUtil.getCurrentUserId();
        BigDecimal productPrice = getProductPrice(dto.getProductId());
        
        // Check if item already exists in cart
        Optional<Cart> existingCart = cartRepository.findByUser_IdAndProductIdAndVariantId(
                userId, dto.getProductId(), dto.getVariantId());

        if (existingCart.isPresent()) {
            Cart cart = existingCart.get();
            int newQuantity = cart.getQuantity() + dto.getQuantity();
            validateQuantity(newQuantity);
            cart.setQuantity(newQuantity);
            cart.setTotalAmount(productPrice.multiply(BigDecimal.valueOf(newQuantity)));
            cart.setFinalAmount(cart.getTotalAmount().subtract(cart.getDiscountAmount()).add(cart.getShippingAmount()));
            cartRepository.save(cart);
            log.info("Updated existing cart item: cartId={}, newQuantity={}", cart.getId(), newQuantity);
        } else {
            Cart newCart = createCart(userId, dto.getProductId(), dto.getVariantId(), dto.getQuantity(), productPrice);
            log.info("Created new cart item: cartId={}", newCart.getId());
        }

        // Get updated cart items
        List<Cart> cartItems = cartRepository.findAllByUser_Id(userId);
        return buildCartResponse(cartItems);
    }

    @Override
    public CartResponseDTO getCart() {
        Long userId = securityUtil.getCurrentUserId();
        List<Cart> cartItems = cartRepository.findAllByUser_Id(userId);
        return buildCartResponse(cartItems);
    }

    @Override
    @Transactional
    public CartResponseDTO updateQuantity(Long itemId, Integer quantity) {
        log.info("Updating cart item quantity: itemId={}, quantityToAdd={}", itemId, quantity);
        
        Cart cart = cartRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        
        validateCartOwnership(cart);
        
        int currentQuantity = cart.getQuantity();
        int newQuantity = currentQuantity + quantity;
        
        validateQuantity(newQuantity);
        
        BigDecimal productPrice = getProductPrice(cart.getProductId());
        cart.setQuantity(newQuantity);
        cart.setTotalAmount(productPrice.multiply(BigDecimal.valueOf(newQuantity)));
        cart.setFinalAmount(cart.getTotalAmount().subtract(cart.getDiscountAmount()).add(cart.getShippingAmount()));
        cartRepository.save(cart);
        
        log.info("Updated cart item quantity: itemId={}, previousQuantity={}, addedQuantity={}, newTotalQuantity={}", 
                itemId, currentQuantity, quantity, newQuantity);
        
        List<Cart> cartItems = cartRepository.findAllByUser_Id(securityUtil.getCurrentUserId());
        return buildCartResponse(cartItems);
    }

    @Override
    @Transactional
    public CartResponseDTO removeItem(Long itemId) {
        log.info("Removing cart item: itemId={}", itemId);
        
        Cart cart = cartRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        
        validateCartOwnership(cart);
        
        cartRepository.delete(cart);
        
        List<Cart> cartItems = cartRepository.findAllByUser_Id(securityUtil.getCurrentUserId());
        return buildCartResponse(cartItems);
    }

    @Override
    @Transactional
    public void clearCart() {
        log.info("Clearing cart for user");
        
        Long userId = securityUtil.getCurrentUserId();
        List<Cart> cartItems = cartRepository.findAllByUser_Id(userId);
        
        cartRepository.deleteAll(cartItems);
    }

    @Override
    public Integer getCartCount() {
        Long userId = securityUtil.getCurrentUserId();
        return cartRepository.findAllByUser_Id(userId).size();
    }

    @Override
    public CartResponseDTO applyCoupon(String code) {
        if (!code.equalsIgnoreCase("SAVE500")) {
            throw new CartException("Invalid coupon code");
        }

        Long userId = securityUtil.getCurrentUserId();
        List<Cart> cartItems = cartRepository.findAllByUser_Id(userId);
        
        for (Cart cart : cartItems) {
            cart.setCouponCode(code);
            cart.setDiscountAmount(BigDecimal.valueOf(500));
            cart.setFinalAmount(cart.getTotalAmount().subtract(cart.getDiscountAmount()).add(cart.getShippingAmount()));
            cartRepository.save(cart);
        }

        return buildCartResponse(cartItems);
    }

    private CartResponseDTO buildCartResponse(List<Cart> cartItems) {
        List<CartItemResponseDTO> itemDTOs = cartItems.stream().map(cart -> {
            CartItemResponseDTO dto = new CartItemResponseDTO();
            dto.setItemId(cart.getId());
            dto.setProductId(cart.getProductId());
            dto.setVariantId(cart.getVariantId());
            dto.setName("Product " + cart.getProductId());
            dto.setImageUrl(getProductImageUrl(cart.getProductId()));
            dto.setPrice(cart.getPrice());
            dto.setOriginalPrice(cart.getPrice().add(BigDecimal.valueOf(300)));
            dto.setQuantity(cart.getQuantity());
            dto.setTotal(cart.getTotalAmount());
            return dto;
        }).toList();

        BigDecimal subtotal = cartItems.stream()
                .map(Cart::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal discount = cartItems.stream()
                .map(Cart::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal delivery = cartItems.stream()
                .map(Cart::getShippingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal finalTotal = subtotal.add(delivery).subtract(discount);

        PriceSummaryDTO summary = new PriceSummaryDTO();
        summary.setSubtotal(subtotal);
        summary.setDiscount(discount);
        summary.setDeliveryCharge(delivery);
        summary.setFinalTotal(finalTotal);

        CartResponseDTO response = new CartResponseDTO();
        response.setItems(itemDTOs);
        response.setPriceSummary(summary);
        response.setCouponApplied(cartItems.isEmpty() ? null : cartItems.get(0).getCouponCode());

        return response;
    }

    private Cart createCart(Long userId, Long productId, Long variantId, Integer quantity, BigDecimal price) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Cart cart = Cart.builder()
                .user(user)
                .sessionId("USER-" + userId)
                .productId(productId)
                .variantId(variantId != null ? variantId : 1L)
                .quantity(quantity != null ? quantity : 1)
                .price(price != null ? price : BigDecimal.ZERO)
                .deliveryType(com.ecommerce.authdemo.dto.Enum.DeliveryType.metro_metro)
                .totalAmount(price != null ? price.multiply(BigDecimal.valueOf(quantity != null ? quantity : 1)) : BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .shippingAmount(BigDecimal.ZERO)
                .finalAmount(price != null ? price.multiply(BigDecimal.valueOf(quantity != null ? quantity : 1)) : BigDecimal.ZERO)
                .currency("USD")
                .build();

        return cartRepository.save(cart);
    }

    private Cart getCartEntity() {
        Long userId = securityUtil.getCurrentUserId();
        return getOrCreateCart(userId);
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUser_Id(userId)
                .orElseGet(() -> createCart(userId, 0L, 1L, 1, BigDecimal.ZERO));
    }

    private void validateAddToCartRequest(AddToCartDTO dto) {
        if (dto == null) {
            throw new CartException("Add to cart request cannot be null");
        }
        if (dto.getProductId() == null || dto.getProductId() <= 0) {
            throw new CartException("Valid product ID is required");
        }
        if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
            throw new CartException("Quantity must be greater than 0");
        }
        validateQuantity(dto.getQuantity());
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new CartException("Quantity must be greater than 0");
        }
        if (quantity > 100) {
            throw new CartException("Maximum quantity allowed is 100");
        }
    }

    private void validateCartOwnership(Cart cart) {
        Long currentUserId = securityUtil.getCurrentUserId();
        if (!cart.getUserId().equals(currentUserId)) {
            throw new CartException("Access denied: You can only modify your own cart");
        }
    }

    private BigDecimal getProductPrice(Long productId) {
        // TODO: Integrate with Product Service to get actual price
        // For now, return a default price based on product ID
        return BigDecimal.valueOf(500 + (productId % 100) * 10);
    }

    private BigDecimal calculateShipping(BigDecimal subtotal) {
        // Free shipping for orders above $1000
        if (subtotal.compareTo(BigDecimal.valueOf(1000)) >= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(99);
    }

    private String getProductImageUrl(Long productId) {
        try {
            String imagePath = null;
            
            // Try to get primary image first
            var primaryImage = productImageRepository.findTopByProductIdAndIsPrimaryTrue(productId);
            if (primaryImage != null && primaryImage.getImagePath() != null) {
                imagePath = primaryImage.getImagePath();
            } else {
                // If no primary image, get first image
                var firstImage = productImageRepository.findFirstByProductId(productId);
                if (firstImage.isPresent() && firstImage.get().getImagePath() != null) {
                    imagePath = firstImage.get().getImagePath();
                }
            }
            
            if (imagePath != null && !imagePath.isEmpty()) {
                // Construct full URL using base URL
                if (imagePath.startsWith("http")) {
                    return imagePath; // Already a full URL
                } else {
                    return publicBaseUrl + (imagePath.startsWith("/") ? imagePath : "/" + imagePath);
                }
            }
            
            // Fallback to placeholder if no images found
            return "https://via.placeholder.com/150";
        } catch (Exception e) {
            log.warn("Error fetching image for product {}: {}", productId, e.getMessage());
            return "https://via.placeholder.com/150";
        }
    }
}
