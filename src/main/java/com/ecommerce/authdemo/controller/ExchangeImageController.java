package com.ecommerce.authdemo.controller;

import com.ecommerce.authdemo.dto.ApiResponse;
import com.ecommerce.authdemo.dto.ExchangeImageRequest;
import com.ecommerce.authdemo.dto.ExchangeImageResponse;
import com.ecommerce.authdemo.service.ExchangeImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exchange-images")
@RequiredArgsConstructor
public class ExchangeImageController {

    private final ExchangeImageService exchangeImageService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExchangeImageResponse>>> getByExchangeId(
            @RequestParam Integer exchangeId) {
        List<ExchangeImageResponse> data = exchangeImageService.getByExchangeId(exchangeId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Exchange images fetched successfully", data));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ExchangeImageResponse>> create(
            @Valid @RequestBody ExchangeImageRequest request) {
        ExchangeImageResponse data = exchangeImageService.create(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Exchange image created successfully", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Integer id) {
        exchangeImageService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Exchange image deleted successfully", "OK"));
    }
}
