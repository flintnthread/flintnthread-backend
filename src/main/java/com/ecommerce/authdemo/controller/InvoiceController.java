package com.ecommerce.authdemo.controller;

import com.ecommerce.authdemo.dto.ApiResponse;
import com.ecommerce.authdemo.dto.InvoiceRequest;
import com.ecommerce.authdemo.dto.InvoiceResponse;
import com.ecommerce.authdemo.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getByOrderId(
            @RequestParam Integer orderId) {
        List<InvoiceResponse> data = invoiceService.getByOrderId(orderId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Invoices fetched successfully", data));
    }

    @GetMapping("/by-number/{invoiceNumber}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getByInvoiceNumber(
            @PathVariable String invoiceNumber) {
        InvoiceResponse data = invoiceService.getByInvoiceNumber(invoiceNumber);
        return ResponseEntity.ok(new ApiResponse<>(true, "Invoice fetched successfully", data));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InvoiceResponse>> create(
            @Valid @RequestBody InvoiceRequest request) {
        InvoiceResponse data = invoiceService.create(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Invoice created successfully", data));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody InvoiceRequest request) {
        InvoiceResponse data = invoiceService.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Invoice updated successfully", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Integer id) {
        invoiceService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Invoice deleted successfully", "OK"));
    }
}
