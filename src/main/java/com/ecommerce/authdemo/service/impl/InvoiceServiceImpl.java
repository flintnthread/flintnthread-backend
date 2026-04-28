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
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;

    @Override
    @Transactional
    public InvoiceResponse create(InvoiceRequest request) {
        Invoice entity = Invoice.builder()
                .orderId(request.getOrderId())
                .invoiceNumber(generateTemporaryInvoiceNumber())
                .invoicePath(normalize(request.getInvoicePath()))
                .build();

        Invoice saved = invoiceRepository.save(entity);
        String generatedInvoiceNumber = generateInvoiceNumber(saved.getId());
        saved.setInvoiceNumber(generatedInvoiceNumber);
        if (saved.getInvoicePath() == null) {
            saved.setInvoicePath(generateInvoiceHtmlFile(generatedInvoiceNumber, saved.getOrderId()));
        }

        return toResponse(invoiceRepository.save(saved));
    }

    @Override
    public List<InvoiceResponse> getByOrderId(Integer orderId) {
        return invoiceRepository.findByOrderIdOrderByCreatedAtDesc(orderId)
                .stream()
                .map(this::ensureInvoiceFileExists)
                .map(this::toResponse)
                .toList();
    }

    @Override
    public InvoiceResponse getByInvoiceNumber(String invoiceNumber) {
        Invoice entity = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        return toResponse(ensureInvoiceFileExists(entity));
    }

    @Override
    public InvoiceResponse update(Integer id, InvoiceRequest request) {
        Invoice entity = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        entity.setOrderId(request.getOrderId());
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

    private String generateTemporaryInvoiceNumber() {
        return "TMP-" + UUID.randomUUID();
    }

    private String generateInvoiceNumber(Integer id) {
        int year = Year.now().getValue();
        return String.format("INV-%d-%06d", year, id);
    }

    private String defaultInvoicePath(String invoiceNumber) {
        return "invoices/Invoice_" + invoiceNumber + ".html";
    }

    private String generateInvoiceHtmlFile(String invoiceNumber, Integer orderId) {
        String relativePath = defaultInvoicePath(invoiceNumber);
        Path invoiceDirectory = invoiceStorageDirectory();
        Path filePath = invoiceDirectory.resolve("Invoice_" + invoiceNumber + ".html");
        String html = """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1" />
                  <title>Invoice %s</title>
                  <style>
                    body { font-family: Arial, sans-serif; margin: 24px; color: #1f2937; }
                    .card { border: 1px solid #e5e7eb; border-radius: 10px; padding: 16px; max-width: 700px; }
                    h1 { margin: 0 0 12px; font-size: 22px; }
                    p { margin: 6px 0; font-size: 14px; }
                    .muted { color: #6b7280; }
                  </style>
                </head>
                <body>
                  <div class="card">
                    <h1>Invoice</h1>
                    <p><strong>Invoice Number:</strong> %s</p>
                    <p><strong>Order ID:</strong> %d</p>
                    <p class="muted">Generated by FlintNThread backend.</p>
                  </div>
                </body>
                </html>
                """.formatted(invoiceNumber, invoiceNumber, orderId);
        try {
            Files.createDirectories(invoiceDirectory);
            Files.writeString(filePath, html, StandardCharsets.UTF_8);
            return relativePath;
        } catch (IOException e) {
            throw new OrderException("Could not generate invoice file");
        }
    }

    private Path invoiceStorageDirectory() {
        return Path.of(System.getProperty("user.dir"), "invoices");
    }

    private Invoice ensureInvoiceFileExists(Invoice entity) {
        String invoiceNumber = normalize(entity.getInvoiceNumber());
        if (invoiceNumber == null) {
            return entity;
        }

        String expectedRelativePath = defaultInvoicePath(invoiceNumber);
        Path expectedFile = invoiceStorageDirectory().resolve("Invoice_" + invoiceNumber + ".html");
        if (Files.exists(expectedFile)) {
            if (!expectedRelativePath.equals(entity.getInvoicePath())) {
                entity.setInvoicePath(expectedRelativePath);
                return invoiceRepository.save(entity);
            }
            return entity;
        }

        String generatedPath = generateInvoiceHtmlFile(invoiceNumber, entity.getOrderId());
        if (!generatedPath.equals(entity.getInvoicePath())) {
            entity.setInvoicePath(generatedPath);
            return invoiceRepository.save(entity);
        }
        return entity;
    }
}
