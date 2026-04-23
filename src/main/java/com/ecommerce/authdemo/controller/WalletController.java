package com.ecommerce.authdemo.controller;

import com.ecommerce.authdemo.dto.ApiResponse;
import com.ecommerce.authdemo.dto.WalletAmountRequest;
import com.ecommerce.authdemo.dto.WalletResponse;
import com.ecommerce.authdemo.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<WalletResponse>> getWallet(@PathVariable Integer userId) {
        WalletResponse data = walletService.getWallet(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Wallet fetched successfully", data));
    }

    @PostMapping("/{userId}/create")
    public ResponseEntity<ApiResponse<String>> createWallet(@PathVariable Integer userId) {
        walletService.createWallet(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Wallet created successfully", "OK"));
    }

    @PostMapping("/{userId}/credit")
    public ResponseEntity<ApiResponse<String>> addMoney(
            @PathVariable Integer userId,
            @Valid @RequestBody WalletAmountRequest request) {
        walletService.addMoney(userId, request.getAmount());
        return ResponseEntity.ok(new ApiResponse<>(true, "Wallet credited successfully", "OK"));
    }

    @PostMapping("/{userId}/debit")
    public ResponseEntity<ApiResponse<String>> deductMoney(
            @PathVariable Integer userId,
            @Valid @RequestBody WalletAmountRequest request) {
        walletService.deductMoney(userId, request.getAmount());
        return ResponseEntity.ok(new ApiResponse<>(true, "Wallet debited successfully", "OK"));
    }
}
