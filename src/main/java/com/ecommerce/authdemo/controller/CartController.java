package com.ecommerce.authdemo.controller;

import com.ecommerce.authdemo.dto.AddToCartDTO;
import com.ecommerce.authdemo.dto.ApiResponse;
import com.ecommerce.authdemo.dto.CartResponseDTO;
import com.ecommerce.authdemo.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartResponseDTO>> add(
            @Valid @RequestBody AddToCartDTO dto) {

        CartResponseDTO response = cartService.addToCart(dto);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Item added to cart", response)
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponseDTO>> getCart() {

        CartResponseDTO response = cartService.getCart();

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Cart fetched", response)
        );
    }

    @PutMapping("/item/{id}")
    public ResponseEntity<ApiResponse<CartResponseDTO>> updateQty(
            @PathVariable Long id,
            @RequestParam Integer quantity) {

        CartResponseDTO response = cartService.updateQuantity(id, quantity);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Quantity updated", response)
        );
    }

    @DeleteMapping("/item/{id}")
    public ResponseEntity<ApiResponse<CartResponseDTO>> remove(
            @PathVariable Long id) {

        CartResponseDTO response = cartService.removeItem(id);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Item removed", response)
        );
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<CartResponseDTO>> clear() {

        cartService.clearCart();
        CartResponseDTO response = cartService.getCart();

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Cart cleared", response)
        );
    }


    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Integer>> count() {

        Integer count = cartService.getCartCount();

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Cart count fetched", count)
        );
    }


    @PostMapping("/apply-coupon")
    public ResponseEntity<ApiResponse<CartResponseDTO>> applyCoupon(
            @RequestParam String code) {

        CartResponseDTO response = cartService.applyCoupon(code);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Coupon applied", response)
        );
    }
}