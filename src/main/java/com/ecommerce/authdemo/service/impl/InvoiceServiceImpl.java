package com.ecommerce.authdemo.service.impl;

import com.ecommerce.authdemo.dto.InvoiceRequest;
import com.ecommerce.authdemo.dto.InvoiceResponse;
import com.ecommerce.authdemo.entity.Invoice;
import com.ecommerce.authdemo.entity.Order;
import com.ecommerce.authdemo.entity.OrderItem;
import com.ecommerce.authdemo.entity.Category;
import com.ecommerce.authdemo.entity.Product;
import com.ecommerce.authdemo.entity.ProductVariant;
import com.ecommerce.authdemo.entity.Seller;
import com.ecommerce.authdemo.entity.SubCategory;
import com.ecommerce.authdemo.exception.OrderException;
import com.ecommerce.authdemo.exception.ResourceNotFoundException;
import com.ecommerce.authdemo.repository.CategoryRepository;
import com.ecommerce.authdemo.repository.InvoiceRepository;
import com.ecommerce.authdemo.repository.OrderItemRepository;
import com.ecommerce.authdemo.repository.OrderRepository;
import com.ecommerce.authdemo.repository.ProductRepository;
import com.ecommerce.authdemo.repository.ProductVariantRepository;
import com.ecommerce.authdemo.repository.SellerRepository;
import com.ecommerce.authdemo.repository.SubCategoryRepository;
import com.ecommerce.authdemo.service.InvoiceService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Pattern SELLER_ID_IN_PATH = Pattern.compile("_seller_(\\d+)\\.html$");
    private static final Pattern GSTIN_STATE_CODE_PATTERN = Pattern.compile("^(\\d{2})");
    private static final Pattern STATE_CODE_PATTERN = Pattern.compile("(\\d{2})");

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final CategoryRepository categoryRepository;

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
        List<Long> sellerIds = resolveSellerIdsForOrder(request.getOrderId());
        if (sellerIds.isEmpty()) {
            sellerIds = List.of();
        }

        Invoice firstSaved = null;
        if (sellerIds.isEmpty()) {
            firstSaved = createInvoiceRecord(request.getOrderId(), normalize(request.getInvoicePath()), null);
        } else {
            for (Long sellerId : sellerIds) {
                Invoice saved = createInvoiceRecord(request.getOrderId(), normalize(request.getInvoicePath()), sellerId);
                if (firstSaved == null) {
                    firstSaved = saved;
                }
            }
        }

        if (firstSaved == null) {
            throw new OrderException("Could not create invoice");
        }
        return toResponse(firstSaved);
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

    private String defaultInvoicePath(String invoiceNumber, Long sellerId) {
        if (sellerId != null) {
            return "invoices/Invoice_" + invoiceNumber + "_seller_" + sellerId + ".html";
        }
        return "invoices/Invoice_" + invoiceNumber + ".html";
    }

    private String generateInvoiceHtmlFile(String invoiceNumber, Integer orderId, Long sellerId) {
        String relativePath = defaultInvoicePath(invoiceNumber, sellerId);
        Path invoiceDirectory = invoiceStorageDirectory();
        String fileName = sellerId == null
                ? "Invoice_" + invoiceNumber + ".html"
                : "Invoice_" + invoiceNumber + "_seller_" + sellerId + ".html";
        Path filePath = invoiceDirectory.resolve(fileName);
        Order order = orderId == null
                ? null
                : orderRepository.findById(Long.valueOf(orderId)).orElse(null);

        String customerName = valueOrDefault(order == null ? null : order.getShippingName(), "Customer");
        String customerPhone = valueOrDefault(order == null ? null : order.getShippingPhone(), "N/A");
        String customerEmail = valueOrDefault(order == null ? null : order.getShippingEmail(), "N/A");
        String customerAddress = buildAddress(order);
        String invoiceDate = formatDateTime(LocalDateTime.now());
        String orderDate = formatDateTime(order == null ? null : order.getCreatedAt());
        SellerSnapshot seller = resolveSellerDetails(orderId, sellerId);
        List<InvoiceLineItem> lineItems = resolveInvoiceLineItems(orderId, sellerId);
        double totalAmountDouble = lineItems.stream()
                .mapToDouble(i -> i.lineTotal() * i.quantity())
                .sum();
        String totalAmount = formatAmount(totalAmountDouble);
        String itemRowsHtml = buildInvoiceItemRowsHtml(lineItems);
        String subtotalBeforeTax = formatAmount(lineItems.stream()
                .mapToDouble(i -> i.unitPrice() * i.quantity())
                .sum());
        String totalTaxAmount = formatAmount(lineItems.stream()
                .mapToDouble(i -> i.taxAmount() * i.quantity())
                .sum());
        String shippingCharge = formatAmount(order == null ? null : order.getShippingAmount());
        double orderTaxAmount = lineItems.stream()
                .mapToDouble(i -> i.taxAmount() * i.quantity())
                .sum();
        String gstRate = "0.00";
        double gstRatePercent = 0.0d;
        double taxableBase = lineItems.stream()
                .mapToDouble(i -> i.unitPrice() * i.quantity())
                .sum();
        if (taxableBase > 0 && orderTaxAmount > 0) {
            gstRatePercent = (orderTaxAmount * 100.0) / taxableBase;
            gstRate = formatAmount(gstRatePercent);
        }

        boolean interStateTransaction = order != null && seller != null
                && isInterStateTransaction(order, seller);

        double totalGstAmount = orderTaxAmount;
        double cgstAmount = interStateTransaction ? 0.0d : totalGstAmount / 2.0d;
        // keep total exact even after rounding
        double sgstAmount = interStateTransaction ? 0.0d : totalGstAmount - cgstAmount;
        double igstAmount = interStateTransaction ? totalGstAmount : 0.0d;
        String gstBreakdownNote = interStateTransaction
                ? "*inter-state transaction - IGST applicable"
                : "*intra-state transaction - CGST and SGST applicable";

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
                        <p class="subheading">GST Breakdown Summary</p>
                        <p><strong>Total GST:</strong> Rs %s</p>
                        <p><strong>Total CGST:</strong> Rs %s</p>
                        <p><strong>Total SGST:</strong> Rs %s</p>
                        <p><strong>Total IGST:</strong> Rs %s</p>
                        <p class="muted">%s</p>
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
                escapeHtml(formatAmount(cgstAmount)),
                escapeHtml(formatAmount(sgstAmount)),
                escapeHtml(formatAmount(igstAmount)),
                escapeHtml(gstBreakdownNote),
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

    private List<InvoiceLineItem> resolveInvoiceLineItems(Integer orderId, Long sellerId) {
        if (orderId == null) {
            return List.of();
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(Long.valueOf(orderId));
        if (sellerId != null) {
            orderItems = orderItems.stream()
                    .filter(item -> Objects.equals(item.getSellerId(), sellerId))
                    .toList();
        }
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

        List<Long> subcategoryIds = productById.values().stream()
                .map(Product::getSubcategoryId)
                .filter(Objects::nonNull)
                .map(Integer::longValue)
                .distinct()
                .toList();
        Map<Long, SubCategory> subcategoryById = subCategoryRepository.findAllById(subcategoryIds).stream()
                .collect(java.util.stream.Collectors.toMap(SubCategory::getId, Function.identity()));

        List<Long> categoryIds = subcategoryById.values().stream()
                .map(SubCategory::getCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, Category> categoryById = categoryRepository.findAllById(categoryIds).stream()
                .collect(java.util.stream.Collectors.toMap(Category::getId, Function.identity()));

        List<Long> variantIds = orderItems.stream()
                .map(OrderItem::getVariantId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, ProductVariant> variantById = productVariantRepository.findAllById(variantIds).stream()
                .collect(java.util.stream.Collectors.toMap(ProductVariant::getId, Function.identity()));

        return orderItems.stream()
                .map(item -> toInvoiceLineItem(
                        item,
                        productById.get(item.getProductId()),
                        variantById.get(item.getVariantId()),
                        subcategoryById,
                        categoryById))
                .toList();
    }

    private InvoiceLineItem toInvoiceLineItem(
            OrderItem item,
            Product product,
            ProductVariant variant,
            Map<Long, SubCategory> subcategoryById,
            Map<Long, Category> categoryById) {
        String productName = valueOrDefault(product == null ? null : product.getName(), "Product " + item.getProductId());
        String color = normalize(variant == null ? null : variant.getColor());
        String size = normalize(variant == null ? null : variant.getSize());
        String description = color == null && size == null
                ? "-"
                : ("Color: " + valueOrDefault(color, "N/A") + ", Size: " + valueOrDefault(size, "N/A"));
        TaxProfile taxProfile = resolveTaxProfile(product, subcategoryById, categoryById);
        String hsnCode = valueOrDefault(
                taxProfile.hsnCode(),
                valueOrDefault(variant == null ? null : variant.getSku(), valueOrDefault(product == null ? null : product.getSku(), "-")));
        int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
        double lineTotal = item.getTotal() == null ? 0.0d : item.getTotal();

        double taxPercent = taxProfile.taxPercent() != null
                ? taxProfile.taxPercent()
                : resolveTaxPercentFromVariant(variant, product);
        /*
         * Business rule:
         * - Order item total is treated as tax-inclusive.
         * - Unit price column should display pre-tax line amount.
         * - Tax is applied once on the complete line total (e.g., qty=2 same product -> one tax amount).
         */
        double unitTotal = quantity > 0 ? (lineTotal / quantity) : lineTotal;
        double preTaxUnitAmount = unitTotal;
        double taxAmountPerUnit = 0.0d;
        if (taxPercent > 0) {
            preTaxUnitAmount = unitTotal * 100.0d / (100.0d + taxPercent);
            taxAmountPerUnit = unitTotal - preTaxUnitAmount;
        }

        return new InvoiceLineItem(
                productName,
                description,
                hsnCode,
                quantity,
                preTaxUnitAmount,
                taxPercent,
                taxAmountPerUnit,
                unitTotal
        );
    }

    private double resolveTaxPercentFromVariant(ProductVariant variant, Product product) {
        if (variant != null && variant.getTaxPercentage() != null) {
            return variant.getTaxPercentage().doubleValue();
        }
        if (product != null && product.getGstPercentage() != null) {
            return product.getGstPercentage().doubleValue();
        }
        return 0.0d;
    }

    private TaxProfile resolveTaxProfile(
            Product product,
            Map<Long, SubCategory> subcategoryById,
            Map<Long, Category> categoryById) {
        if (product == null || product.getSubcategoryId() == null) {
            return new TaxProfile(null, null);
        }

        SubCategory subCategory = subcategoryById.get(product.getSubcategoryId().longValue());
        if (subCategory == null) {
            return new TaxProfile(null, null);
        }

        TaxProfile materialSlabTax = resolveTaxFromMaterialSlabs(subCategory.getMaterialSlabs());
        String hsnCode = materialSlabTax.hsnCode();
        Double gstPercent = materialSlabTax.taxPercent() != null
                ? materialSlabTax.taxPercent()
                : (subCategory.getGstPercentage() == null ? null : subCategory.getGstPercentage().doubleValue());

        if (subCategory.getCategoryId() != null) {
            Category category = categoryById.get(subCategory.getCategoryId());
            if (category != null) {
                if (hsnCode == null) {
                    hsnCode = normalize(category.getHsnCode());
                }
            }
            if (gstPercent == null && category != null) {
                gstPercent = category.getGstPercentage();
            }
        }

        return new TaxProfile(hsnCode, gstPercent);
    }

    private TaxProfile resolveTaxFromMaterialSlabs(String materialSlabsRaw) {
        String normalized = normalize(materialSlabsRaw);
        if (normalized == null) {
            return new TaxProfile(null, null);
        }

        try {
            JsonNode root = OBJECT_MAPPER.readTree(normalized);
            String hsnCode = findTextByKeys(root, "hsnCode", "hsn_code", "hsn");
            Double taxPercent = findNumberByKeys(root, "gstPercentage", "gst_percentage", "taxPercentage", "tax_percentage", "gst", "tax");
            return new TaxProfile(normalize(hsnCode), taxPercent);
        } catch (Exception ignored) {
            return new TaxProfile(null, null);
        }
    }

    private String findTextByKeys(JsonNode node, String... keys) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isObject()) {
            for (String key : keys) {
                JsonNode direct = node.get(key);
                if (direct != null && !direct.isNull() && direct.isValueNode()) {
                    String value = normalize(direct.asText());
                    if (value != null) {
                        return value;
                    }
                }
            }
            java.util.Iterator<JsonNode> iterator = node.elements();
            while (iterator.hasNext()) {
                String value = findTextByKeys(iterator.next(), keys);
                if (value != null) {
                    return value;
                }
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                String value = findTextByKeys(child, keys);
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

    private Double findNumberByKeys(JsonNode node, String... keys) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isObject()) {
            for (String key : keys) {
                JsonNode direct = node.get(key);
                if (direct != null && !direct.isNull() && direct.isValueNode()) {
                    if (direct.isNumber()) {
                        return direct.asDouble();
                    }
                    String raw = normalize(direct.asText());
                    if (raw != null) {
                        try {
                            return Double.parseDouble(raw);
                        } catch (NumberFormatException ignored) {
                            // continue searching nested nodes
                        }
                    }
                }
            }
            java.util.Iterator<JsonNode> iterator = node.elements();
            while (iterator.hasNext()) {
                Double value = findNumberByKeys(iterator.next(), keys);
                if (value != null) {
                    return value;
                }
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                Double value = findNumberByKeys(child, keys);
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
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

    private SellerSnapshot resolveSellerDetails(Integer orderId, Long targetSellerId) {
        if (orderId == null) {
            return defaultSellerSnapshot();
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(Long.valueOf(orderId));
        if (orderItems == null || orderItems.isEmpty()) {
            return defaultSellerSnapshot();
        }

        Long sellerId = targetSellerId != null
                ? targetSellerId
                : orderItems.stream()
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

    private boolean isInterStateTransaction(Order order, SellerSnapshot seller) {
        String shippingStateCode = extractFirstStateCode(order.getShippingState());
        String sellerStateCode = extractStateCodeFromGstin(seller.gstin());

        // If we can't determine, default to intra-state so invoice doesn't suddenly show IGST.
        if (shippingStateCode == null || sellerStateCode == null) {
            return true;
        }
        return !shippingStateCode.equals(sellerStateCode);
    }

    private String extractStateCodeFromGstin(String gstin) {
        String normalized = normalize(gstin);
        if (normalized == null) {
            return null;
        }
        Matcher m = GSTIN_STATE_CODE_PATTERN.matcher(normalized);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    private String extractFirstStateCode(String shippingState) {
        String normalized = normalize(shippingState);
        if (normalized == null) {
            return null;
        }
        Matcher m = STATE_CODE_PATTERN.matcher(normalized);
        if (m.find()) {
            return m.group(1);
        }
        return null;
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

    private record TaxProfile(
            String hsnCode,
            Double taxPercent
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

        Long sellerId = resolveSellerIdFromInvoicePath(entity.getInvoicePath());
        String expectedRelativePath = defaultInvoicePath(invoiceNumber, sellerId);
        String expectedFileName = sellerId == null
                ? "Invoice_" + invoiceNumber + ".html"
                : "Invoice_" + invoiceNumber + "_seller_" + sellerId + ".html";
        Path expectedFile = invoiceStorageDirectory().resolve(expectedFileName);
        if (Files.exists(expectedFile)) {
            if (!expectedRelativePath.equals(entity.getInvoicePath())) {
                entity.setInvoicePath(expectedRelativePath);
                return invoiceRepository.save(entity);
            }
            return entity;
        }

        String generatedPath = generateInvoiceHtmlFile(invoiceNumber, entity.getOrderId(), sellerId);
        if (!generatedPath.equals(entity.getInvoicePath())) {
            entity.setInvoicePath(generatedPath);
            return invoiceRepository.save(entity);
        }
        return entity;
    }

    private Invoice createInvoiceRecord(Integer orderId, String explicitPath, Long sellerId) {
        Invoice entity = Invoice.builder()
                .orderId(orderId)
                .invoiceNumber(generateTemporaryInvoiceNumber())
                .invoicePath(explicitPath)
                .build();

        Invoice saved = invoiceRepository.save(entity);
        String generatedInvoiceNumber = generateInvoiceNumber(saved.getId());
        saved.setInvoiceNumber(generatedInvoiceNumber);
        if (saved.getInvoicePath() == null) {
            saved.setInvoicePath(generateInvoiceHtmlFile(generatedInvoiceNumber, saved.getOrderId(), sellerId));
        }
        return invoiceRepository.save(saved);
    }

    private List<Long> resolveSellerIdsForOrder(Integer orderId) {
        if (orderId == null) {
            return List.of();
        }
        return orderItemRepository.findByOrderId(Long.valueOf(orderId)).stream()
                .map(OrderItem::getSellerId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    private Long resolveSellerIdFromInvoicePath(String invoicePath) {
        String path = normalize(invoicePath);
        if (path == null) {
            return null;
        }
        Matcher matcher = SELLER_ID_IN_PATH.matcher(path);
        if (!matcher.find()) {
            return null;
        }
        try {
            return Long.valueOf(matcher.group(1));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
