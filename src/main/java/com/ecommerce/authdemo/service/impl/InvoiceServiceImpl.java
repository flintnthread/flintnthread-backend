package com.ecommerce.authdemo.service.impl;

import com.ecommerce.authdemo.dto.InvoiceRequest;
import com.ecommerce.authdemo.dto.InvoiceResponse;
import com.ecommerce.authdemo.entity.Invoice;
import com.ecommerce.authdemo.entity.Order;
import com.ecommerce.authdemo.entity.OrderItem;
import com.ecommerce.authdemo.entity.Product;
import com.ecommerce.authdemo.entity.ProductVariant;
import com.ecommerce.authdemo.entity.Seller;
import com.ecommerce.authdemo.exception.OrderException;
import com.ecommerce.authdemo.exception.ResourceNotFoundException;
import com.ecommerce.authdemo.repository.InvoiceRepository;
import com.ecommerce.authdemo.repository.OrderItemRepository;
import com.ecommerce.authdemo.repository.OrderRepository;
import com.ecommerce.authdemo.repository.ProductRepository;
import com.ecommerce.authdemo.repository.ProductVariantRepository;
import com.ecommerce.authdemo.repository.SellerRepository;
import com.ecommerce.authdemo.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    @Value("${invoice.seller.name:Flint & Thread (India) Pvt. Ltd.}")
    private String sellerName;

    @Value("${invoice.seller.address:India}")
    private String sellerAddress;

    @Value("${invoice.seller.phone:N/A}")
    private String sellerPhone;

    @Value("${invoice.seller.email:N/A}")
    private String sellerEmail;

    @Value("${invoice.seller.gstin:N/A}")
    private String sellerGstin;

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
        Order order = orderId == null
                ? null
                : orderRepository.findById(Long.valueOf(orderId)).orElse(null);

        String customerName = valueOrDefault(order == null ? null : order.getShippingName(), "Customer");
        String customerPhone = valueOrDefault(order == null ? null : order.getShippingPhone(), "N/A");
        String customerEmail = valueOrDefault(order == null ? null : order.getShippingEmail(), "N/A");
        String customerAddress = buildAddress(order);
        String invoiceDate = formatDateTime(LocalDateTime.now());
        String orderDate = formatDateTime(order == null ? null : order.getCreatedAt());
        String totalAmount = formatAmount(order == null ? null : order.getTotalAmount());
        SellerSnapshot seller = resolveSellerDetails(orderId);
        List<InvoiceLineItem> lineItems = resolveInvoiceLineItems(orderId);
        String itemRowsHtml = buildInvoiceItemRowsHtml(lineItems);
        String subtotalBeforeTax = formatAmount(lineItems.stream().mapToDouble(InvoiceLineItem::lineTotal).sum());
        String totalTaxAmount = formatAmount(lineItems.stream().mapToDouble(InvoiceLineItem::taxAmount).sum());
        String shippingCharge = formatAmount(order == null ? null : order.getShippingAmount());
        double orderTaxAmount = order != null && order.getTaxAmount() != null ? order.getTaxAmount() : 0.0d;
        String gstRate = "0.00";
        double taxableBase = lineItems.stream().mapToDouble(i -> i.unitPrice() * i.quantity()).sum();
        if (taxableBase > 0 && orderTaxAmount > 0) {
            gstRate = formatAmount((orderTaxAmount * 100.0) / taxableBase);
        }

        String html = """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1" />
                  <title>Invoice %s</title>
                  <style>
                    body { font-family: Arial, sans-serif; margin: 0; background: #f3f4f6; color: #1f2937; }
                    .page { max-width: 850px; margin: 20px auto; background: #fff; border: 1px solid #e5e7eb; border-radius: 12px; overflow: hidden; }
                    .section { padding: 16px 20px; }
                    .header { display: flex; justify-content: space-between; gap: 16px; border-bottom: 1px solid #e5e7eb; }
                    .company h1 { margin: 0 0 8px; font-size: 26px; }
                    .company p { margin: 4px 0; font-size: 14px; }
                    .invoice-meta { min-width: 260px; border: 1px solid #e5e7eb; border-radius: 10px; padding: 12px; }
                    .invoice-meta h2 { margin: 0 0 8px; font-size: 20px; color: #b91c1c; text-transform: uppercase; letter-spacing: 0.5px; }
                    .invoice-meta p { margin: 4px 0; font-size: 13px; }
                    .heading { margin: 0 0 10px; font-size: 24px; }
                    .subheading { margin: 0 0 6px; font-size: 14px; text-transform: uppercase; color: #6b7280; }
                    .party-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
                    .party-box { border: 1px solid #e5e7eb; border-radius: 10px; padding: 12px; }
                    .party-box h3 { margin: 0 0 8px; font-size: 24px; }
                    .party-box p { margin: 4px 0; font-size: 13px; }
                    .items-table { width: 100%%; border-collapse: collapse; margin-top: 12px; }
                    .items-table th { background: #123763; color: #fff; font-size: 12px; text-align: left; padding: 8px; text-transform: uppercase; }
                    .items-table td { font-size: 12px; padding: 8px; border-bottom: 1px solid #e5e7eb; vertical-align: top; }
                    .items-table td.right { text-align: right; }
                    .item-meta { color: #6b7280; font-size: 11px; margin-top: 3px; }
                    .total { text-align: right; border-top: 1px solid #e5e7eb; margin-top: 14px; padding-top: 12px; }
                    .total p { margin: 6px 0; }
                    .grand { font-size: 28px; color: #92400e; font-weight: 700; }
                    .muted { color: #6b7280; }
                  </style>
                </head>
                <body>
                  <div class="page">
                    <div class="section header">
                      <div class="company">
                        <h1>%s</h1>
                        <p>%s</p>
                        <p><strong>Phone:</strong> %s</p>
                        <p><strong>Email:</strong> %s</p>
                        <p><strong>GSTIN:</strong> %s</p>
                      </div>
                      <div class="invoice-meta">
                        <h2>Invoice</h2>
                        <p><strong>Invoice:</strong> %s</p>
                        <p><strong>Order ID:</strong> %d</p>
                        <p><strong>Date:</strong> %s</p>
                        <p><strong>Order Date:</strong> %s</p>
                      </div>
                    </div>

                    <div class="section">
                      <p class="subheading">Sold By</p>
                      <p><strong>%s</strong></p>
                      <p>%s</p>
                      <p><strong>Phone:</strong> %s</p>
                      <p><strong>Email:</strong> %s</p>
                    </div>

                    <div class="section">
                      <table class="items-table">
                        <thead>
                          <tr>
                            <th>Item Description</th>
                            <th>HSN Code</th>
                            <th>Qty</th>
                            <th>Unit Price</th>
                            <th>Tax %%</th>
                            <th>Tax Amount</th>
                            <th>Total</th>
                          </tr>
                        </thead>
                        <tbody>
                          %s
                        </tbody>
                      </table>

                      <div class="party-grid">
                        <div class="party-box">
                          <h3>Bill To:</h3>
                          <p><strong>%s</strong></p>
                          <p>%s</p>
                          <p><strong>Phone:</strong> %s</p>
                          <p><strong>Email:</strong> %s</p>
                        </div>
                        <div class="party-box">
                          <h3>Ship To:</h3>
                          <p><strong>%s</strong></p>
                          <p>%s</p>
                          <p><strong>Phone:</strong> %s</p>
                          <p><strong>Email:</strong> %s</p>
                        </div>
                      </div>
                      <div class="total">
                        <p><strong>Subtotal (Before Tax):</strong> Rs %s</p>
                        <p><strong>Total Tax:</strong> Rs %s</p>
                        <p><strong>IGST @ %s%%:</strong> Rs %s</p>
                        <p><strong>Shipping Charges:</strong> Rs %s</p>
                        <p><strong>Grand Total:</strong></p>
                        <p class="grand">Rs %s</p>
                      </div>
                      <p class="muted">Generated by FlintNThread backend.</p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(
                escapeHtml(invoiceNumber),
                escapeHtml(sellerName),
                escapeHtml(sellerAddress),
                escapeHtml(sellerPhone),
                escapeHtml(sellerEmail),
                escapeHtml(sellerGstin),
                escapeHtml(invoiceNumber),
                orderId,
                escapeHtml(invoiceDate),
                escapeHtml(orderDate),
                escapeHtml(seller.name()),
                escapeHtml(seller.address()),
                escapeHtml(seller.phone()),
                escapeHtml(seller.email()),
                itemRowsHtml,
                escapeHtml(customerName),
                escapeHtml(customerAddress),
                escapeHtml(customerPhone),
                escapeHtml(customerEmail),
                escapeHtml(customerName),
                escapeHtml(customerAddress),
                escapeHtml(customerPhone),
                escapeHtml(customerEmail),
                escapeHtml(subtotalBeforeTax),
                escapeHtml(totalTaxAmount),
                escapeHtml(gstRate),
                escapeHtml(formatAmount(orderTaxAmount)),
                escapeHtml(shippingCharge),
                escapeHtml(totalAmount)
        );
        try {
            Files.createDirectories(invoiceDirectory);
            Files.writeString(filePath, html, StandardCharsets.UTF_8);
            return relativePath;
        } catch (IOException e) {
            throw new OrderException("Could not generate invoice file");
        }
    }

    private String buildAddress(Order order) {
        if (order == null) {
            return "Address not available";
        }

        String addressLine = normalize(order.getShippingAddress1());
        String city = normalize(order.getShippingCity());
        String state = normalize(order.getShippingState());
        String pincode = normalize(order.getShippingPincode());
        String cityStatePin = String.join(", ",
                java.util.stream.Stream.of(city, state, pincode)
                        .filter(value -> value != null && !value.isBlank())
                        .toList());

        if (addressLine == null && cityStatePin.isBlank()) {
            return "Address not available";
        }
        if (addressLine == null) {
            return cityStatePin;
        }
        if (cityStatePin.isBlank()) {
            return addressLine;
        }
        return addressLine + ", " + cityStatePin;
    }

    private String formatDateTime(LocalDateTime value) {
        if (value == null) {
            return "N/A";
        }
        return value.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy, h:mm a", Locale.ENGLISH));
    }

    private String formatAmount(Double value) {
        if (value == null) {
            return "0.00";
        }
        return String.format(Locale.ENGLISH, "%.2f", value);
    }

    private String valueOrDefault(String value, String defaultValue) {
        String normalized = normalize(value);
        return normalized == null ? defaultValue : normalized;
    }

    private List<InvoiceLineItem> resolveInvoiceLineItems(Integer orderId) {
        if (orderId == null) {
            return List.of();
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(Long.valueOf(orderId));
        if (orderItems == null || orderItems.isEmpty()) {
            return List.of();
        }

        List<Long> productIds = orderItems.stream()
                .map(OrderItem::getProductId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, Product> productById = productRepository.findAllById(productIds).stream()
                .collect(java.util.stream.Collectors.toMap(Product::getId, Function.identity()));

        List<Long> variantIds = orderItems.stream()
                .map(OrderItem::getVariantId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, ProductVariant> variantById = productVariantRepository.findAllById(variantIds).stream()
                .collect(java.util.stream.Collectors.toMap(ProductVariant::getId, Function.identity()));

        return orderItems.stream()
                .map(item -> toInvoiceLineItem(item, productById.get(item.getProductId()), variantById.get(item.getVariantId())))
                .toList();
    }

    private InvoiceLineItem toInvoiceLineItem(OrderItem item, Product product, ProductVariant variant) {
        String productName = valueOrDefault(product == null ? null : product.getName(), "Product " + item.getProductId());
        String color = normalize(variant == null ? null : variant.getColor());
        String size = normalize(variant == null ? null : variant.getSize());
        String description = color == null && size == null
                ? "-"
                : ("Color: " + valueOrDefault(color, "N/A") + ", Size: " + valueOrDefault(size, "N/A"));
        String hsnCode = valueOrDefault(variant == null ? null : variant.getSku(), valueOrDefault(product == null ? null : product.getSku(), "-"));
        int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
        double lineTotal = item.getTotal() == null ? 0.0d : item.getTotal();
        double unitPrice = quantity > 0 ? lineTotal / quantity : (item.getPrice() == null ? 0.0d : item.getPrice());

        double taxPercent = product != null && product.getGstPercentage() != null
                ? product.getGstPercentage().doubleValue()
                : resolveTaxPercentFromVariant(variant);
        double taxableValue = unitPrice * quantity;
        double taxAmount = taxPercent > 0 ? (taxableValue * taxPercent / 100.0d) : 0.0d;

        return new InvoiceLineItem(productName, description, hsnCode, quantity, unitPrice, taxPercent, taxAmount, lineTotal);
    }

    private double resolveTaxPercentFromVariant(ProductVariant variant) {
        if (variant == null || variant.getTaxPercentage() == null) {
            return 0.0d;
        }
        BigDecimal percentage = variant.getTaxPercentage();
        return percentage.doubleValue();
    }

    private String buildInvoiceItemRowsHtml(List<InvoiceLineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            return """
                    <tr>
                      <td colspan="7" class="right">No items found for this order.</td>
                    </tr>
                    """;
        }

        StringBuilder rows = new StringBuilder();
        for (InvoiceLineItem item : lineItems) {
            rows.append("""
                    <tr>
                      <td><strong>%s</strong><div class="item-meta">%s</div></td>
                      <td>%s</td>
                      <td class="right">%d</td>
                      <td class="right">Rs %s</td>
                      <td class="right">%s%%</td>
                      <td class="right">Rs %s</td>
                      <td class="right">Rs %s</td>
                    </tr>
                    """.formatted(
                    escapeHtml(item.productName()),
                    escapeHtml(item.description()),
                    escapeHtml(item.hsnCode()),
                    item.quantity(),
                    escapeHtml(formatAmount(item.unitPrice())),
                    escapeHtml(formatAmount(item.taxPercent())),
                    escapeHtml(formatAmount(item.taxAmount())),
                    escapeHtml(formatAmount(item.lineTotal()))
            ));
        }
        return rows.toString();
    }

    private SellerSnapshot resolveSellerDetails(Integer orderId) {
        if (orderId == null) {
            return defaultSellerSnapshot();
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(Long.valueOf(orderId));
        if (orderItems == null || orderItems.isEmpty()) {
            return defaultSellerSnapshot();
        }

        Long sellerId = orderItems.stream()
                .map(OrderItem::getSellerId)
                .filter(id -> id != null && id > 0)
                .findFirst()
                .orElse(null);

        if (sellerId == null) {
            return defaultSellerSnapshot();
        }

        return sellerRepository.findById(sellerId)
                .map(this::toSellerSnapshot)
                .orElseGet(this::defaultSellerSnapshot);
    }

    private SellerSnapshot toSellerSnapshot(Seller seller) {
        String sellerDisplayName = normalize(seller.getBusinessName());
        if (sellerDisplayName == null) {
            sellerDisplayName = String.join(" ",
                    java.util.stream.Stream.of(normalize(seller.getFirstName()), normalize(seller.getLastName()))
                            .filter(value -> value != null && !value.isBlank())
                            .toList());
        }

        return new SellerSnapshot(
                valueOrDefault(sellerDisplayName, sellerName),
                valueOrDefault(normalize(seller.getAddress()), sellerAddress),
                valueOrDefault(normalize(seller.getMobileNumber()), sellerPhone),
                valueOrDefault(normalize(seller.getEmail()), sellerEmail),
                sellerGstin
        );
    }

    private SellerSnapshot defaultSellerSnapshot() {
        return new SellerSnapshot(sellerName, sellerAddress, sellerPhone, sellerEmail, sellerGstin);
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private record SellerSnapshot(
            String name,
            String address,
            String phone,
            String email,
            String gstin
    ) {
    }

    private record InvoiceLineItem(
            String productName,
            String description,
            String hsnCode,
            int quantity,
            double unitPrice,
            double taxPercent,
            double taxAmount,
            double lineTotal
    ) {
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
