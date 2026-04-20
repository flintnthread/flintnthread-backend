package com.ecommerce.authdemo.controller;

import com.ecommerce.authdemo.dto.ApiResponse;
import com.ecommerce.authdemo.dto.OrderResponseDTO;
import com.ecommerce.authdemo.dto.PlaceOrderRequestDTO;
import com.ecommerce.authdemo.service.OrderService;
import com.ecommerce.authdemo.exception.OrderException;
import com.ecommerce.authdemo.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin("*")
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/place")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> placeOrder(
            @Valid @RequestBody PlaceOrderRequestDTO dto) {
        
        log.info("Place order request: addressId={}, paymentMethod={}", 
                dto.getAddressId(), dto.getPaymentMethod());
        
        try {
            OrderResponseDTO response = orderService.placeOrder(dto);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Order placed successfully", response)
            );
        } catch (OrderException e) {
            log.error("Order error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found: {}", e.getMessage());
            return ResponseEntity.status(404)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error placing order: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Failed to place order", null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> getOrders(
            @RequestParam(required = false) String status) {
        
        log.info("Fetch orders request: status={}", status);
        
        try {
            List<OrderResponseDTO> orders = orderService.getUserOrders(status);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Orders fetched successfully", orders)
            );
        } catch (Exception e) {
            log.error("Error fetching orders: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Failed to fetch orders", List.of()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> getOrderDetails(
            @PathVariable Long id) {
        
        log.info("Fetch order details request: orderId={}", id);
        
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Valid order ID is required", null));
        }
        
        try {
            OrderResponseDTO order = orderService.getOrderDetails(id);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Order details fetched successfully", order)
            );
        } catch (ResourceNotFoundException e) {
            log.error("Order not found: {}", e.getMessage());
            return ResponseEntity.status(404)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (OrderException e) {
            log.error("Order access error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error fetching order details: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Failed to fetch order details", null));
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<String>> cancelOrder(@PathVariable Long id) {
        
        log.info("Cancel order request: orderId={}", id);
        
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Valid order ID is required", null));
        }
        
        try {
            orderService.cancelOrder(id);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Order cancelled successfully", null)
            );
        } catch (ResourceNotFoundException e) {
            log.error("Order not found: {}", e.getMessage());
            return ResponseEntity.status(404)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (OrderException e) {
            log.error("Order cancellation error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error cancelling order: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Failed to cancel order", null));
        }
    }
}