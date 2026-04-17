package com.ecommerce.authdemo.controller;

import com.ecommerce.authdemo.dto.ApiResponse;
import com.ecommerce.authdemo.dto.OrderResponseDTO;
import com.ecommerce.authdemo.dto.PlaceOrderRequestDTO;
import com.ecommerce.authdemo.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/place")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> placeOrder(
            @Valid @RequestBody PlaceOrderRequestDTO dto) {

        OrderResponseDTO response = orderService.placeOrder(dto);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Order placed successfully", response)
        );
    }


    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> getOrders() {

        List<OrderResponseDTO> orders = orderService.getUserOrders();

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Orders fetched successfully", orders)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> getOrderDetails(
            @PathVariable Long id) {

        OrderResponseDTO order = orderService.getOrderDetails(id);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Order details fetched", order)
        );
    }
}