package com.ecommerce.authdemo.controller;

import com.ecommerce.authdemo.dto.ApiResponse;
import com.ecommerce.authdemo.dto.ReturnImageRequest;
import com.ecommerce.authdemo.dto.ReturnImageResponse;
import com.ecommerce.authdemo.service.ReturnImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/return-images")
@RequiredArgsConstructor
public class ReturnImageController {

    private final ReturnImageService returnImageService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReturnImageResponse>>> getByReturnId(
            @RequestParam Integer returnId) {
        List<ReturnImageResponse> data = returnImageService.getByReturnId(returnId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Return images fetched successfully", data));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReturnImageResponse>> create(
            @Valid @RequestBody ReturnImageRequest request) {
        ReturnImageResponse data = returnImageService.create(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Return image created successfully", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Integer id) {
        returnImageService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Return image deleted successfully", "OK"));
    }
}
