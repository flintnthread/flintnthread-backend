package com.ecommerce.authdemo.exception;

import com.ecommerce.authdemo.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntime(RuntimeException ex) {

        return ResponseEntity.badRequest().body(
                new ApiResponse<>(false, ex.getMessage(), null)
        );
    }

}