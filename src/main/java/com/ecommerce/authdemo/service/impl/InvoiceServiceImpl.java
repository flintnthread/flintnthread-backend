package com.ecommerce.authdemo.service.impl;

import com.ecommerce.authdemo.dto.InvoiceRequest;
import com.ecommerce.authdemo.dto.InvoiceResponse;
import com.ecommerce.authdemo.entity.Invoice;
import com.ecommerce.authdemo.exception.OrderException;
import com.ecommerce.authdemo.exception.ResourceNotFoundException;
import com.ecommerce.authdemo.repository.InvoiceRepository;
import com.ecommerce.authdemo.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;

    @Override
    public InvoiceResponse create(InvoiceRequest request) {
        if (invoiceRepository.findByInvoiceNumber(request.getInvoiceNumber().trim()).isPresent()) {
            throw new OrderException("Invoice number already exists");
        }

        Invoice entity = Invoice.builder()
                .orderId(request.getOrderId())
                .invoiceNumber(request.getInvoiceNumber().trim())
                .invoicePath(normalize(request.getInvoicePath()))
                .build();

        return toResponse(invoiceRepository.save(entity));
    }

    @Override
    public List<InvoiceResponse> getByOrderId(Integer orderId) {
        return invoiceRepository.findByOrderIdOrderByCreatedAtDesc(orderId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public InvoiceResponse getByInvoiceNumber(String invoiceNumber) {
        Invoice entity = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        return toResponse(entity);
    }

    @Override
    public InvoiceResponse update(Integer id, InvoiceRequest request) {
        Invoice entity = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        String targetNumber = request.getInvoiceNumber().trim();
        invoiceRepository.findByInvoiceNumber(targetNumber)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new OrderException("Invoice number already exists");
                });

        entity.setOrderId(request.getOrderId());
        entity.setInvoiceNumber(targetNumber);
        entity.setInvoicePath(normalize(request.getInvoicePath()));
        return toResponse(invoiceRepository.save(entity));
    }

    @Override
    public void delete(Integer id) {
        Invoice entity = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        invoiceRepository.delete(entity);
    }

    private InvoiceResponse toResponse(Invoice entity) {
        return InvoiceResponse.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .invoiceNumber(entity.getInvoiceNumber())
                .invoicePath(entity.getInvoicePath())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private String normalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
