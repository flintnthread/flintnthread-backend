package com.ecommerce.authdemo.service;

import com.ecommerce.authdemo.dto.ShiprocketShipmentResult;
import com.ecommerce.authdemo.entity.Order;
import com.ecommerce.authdemo.entity.OrderItem;
import com.ecommerce.authdemo.entity.Seller;
import com.ecommerce.authdemo.repository.OrderItemRepository;
import com.ecommerce.authdemo.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShiprocketService {

    private static final Logger log = LoggerFactory.getLogger(ShiprocketService.class);

    private final RestTemplate restTemplate;
    private final OrderService orderService;
    private final OrderItemRepository orderItemRepository;
    private final SellerRepository sellerRepository;

    @Value("${shiprocket.email}")
    private String email;

    @Value("${shiprocket.password}")
    private String password;

    @Value("${shiprocket.api.base-url:https://apiv2.shiprocket.in}")
    private String apiBaseUrl;

    @Value("${shiprocket.pickup-location:Primary}")
    private String pickupLocation;

    @Value("${shiprocket.pickup-location-by-seller:}")
    private String pickupLocationBySellerRaw;

    public String getToken() {
        String url = apiBaseUrl + "/v1/external/auth/login";
        log.info("[SHIPROCKET] POST {} (auth login)", url);
        try {
            Map<String, String> body = Map.of("email", email, "password", password);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, body, Map.class);

            if (response.getBody() != null && response.getBody().containsKey("token")) {
                log.info("[SHIPROCKET] auth OK (token received)");
                return (String) response.getBody().get("token");
            }
            log.error("[SHIPROCKET] auth failed: response had no token");
            throw new RuntimeException("Failed to obtain token from Shiprocket API");
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("[SHIPROCKET] auth HTTP {} body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to authenticate with Shiprocket", e);
        } catch (Exception e) {
            log.error("[SHIPROCKET] auth error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to authenticate with Shiprocket", e);
        }
    }

    public ShiprocketShipmentResult createShipment(Order order) {
        String orderNumber = order.getOrderNumber();
        log.info("[SHIPROCKET] createShipment START orderNumber={} orderId={} paymentStatus={}",
                orderNumber, order.getId(), order.getPaymentStatus());

        try {
            String token = getToken();
            String url = apiBaseUrl + "/v1/external/orders/create/adhoc";
            log.info("[SHIPROCKET] POST {} (adhoc create)", url);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> payload = buildShipmentPayload(order, resolvePickupLocation(order, token));
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("[SHIPROCKET] adhoc non-success http={} body={}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("Shiprocket adhoc returned non-success");
            }

            Map<String, Object> responseBody = response.getBody();
            Object shipmentIdVal = pickFirst(responseBody,
                    "shipment_id", "shipmentId", "data.shipment_id", "data.shipmentId");
            if (shipmentIdVal == null) {
                String apiMessage = stringOrNull(pickFirst(responseBody, "message", "data.message", "error", "errors"));
                log.error("[SHIPROCKET] adhoc missing shipment_id orderNumber={} body={}", orderNumber, responseBody);
                throw new RuntimeException("Shiprocket create-order did not return shipment_id"
                        + (apiMessage != null ? " message=" + apiMessage : ""));
            }

            String shipmentId = String.valueOf(shipmentIdVal);
            String awbRaw = stringOrNull(pickFirst(responseBody,
                    "awb_code", "awb", "data.awb_code", "data.awb", "data.awbCode"));
            awbRaw = awbRaw == null ? "" : awbRaw.trim();
            String awb = awbRaw.isEmpty() ? null : awbRaw;

            String trackingUrl = stringOrNull(pickFirst(responseBody,
                    "tracking_url", "trackingUrl", "data.tracking_url", "data.trackingUrl"));
            trackingUrl = trackingUrl == null ? null : trackingUrl.trim();
            if (trackingUrl == null || trackingUrl.isEmpty()) {
                if (awb != null) {
                    trackingUrl = "https://shiprocket.co/tracking/" + awb;
                } else {
                    trackingUrl = "https://app.shiprocket.in/seller/orders/all-shipment/" + shipmentId;
                }
            }

            String courier = responseBody.containsKey("courier_name") && responseBody.get("courier_name") != null
                    ? responseBody.get("courier_name").toString()
                    : "Shiprocket";

            String srState = (awb != null && !awb.isBlank()) ? "awb_assigned" : "order_created";
            orderService.updateShipment(orderNumber, awb, courier, trackingUrl, srState);

            log.info("[SHIPROCKET] createShipment DONE orderNumber={} shipmentId={} awb={} shiprocketStatus={}",
                    orderNumber, shipmentId, awb, srState);

            return ShiprocketShipmentResult.builder()
                    .shipmentId(shipmentId)
                    .awbCode(awb)
                    .trackingUrl(trackingUrl)
                    .courierName(courier)
                    .build();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("[SHIPROCKET] adhoc HTTP {} orderNumber={} body={}",
                    e.getStatusCode(), orderNumber, e.getResponseBodyAsString());
            orderService.markShiprocketCreateFailed(orderNumber, e.getMessage());
            throw new RuntimeException("Shiprocket API error: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("[SHIPROCKET] createShipment FAILED orderNumber={}: {}", orderNumber, e.getMessage(), e);
            orderService.markShiprocketCreateFailed(orderNumber, e.getMessage());
            throw new RuntimeException("Shipment creation failed", e);
        }
    }

    /**
     * Shiprocket's {@code pickup_location} must be a registered pickup nickname.
     * Priority: explicit seller mapping -> dynamic Shiprocket pickup list match -> configured default.
     */
    private String resolvePickupLocation(Order order, String token) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        List<Long> sellerIds = items.stream()
                .map(OrderItem::getSellerId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (sellerIds.size() > 1) {
            log.warn("[SHIPROCKET] order {} has multiple seller_ids {}; using first line item's seller for pickup",
                    order.getOrderNumber(), sellerIds);
        }

        Optional<Long> sellerIdOpt = items.stream()
                .map(OrderItem::getSellerId)
                .filter(Objects::nonNull)
                .findFirst();

        if (sellerIdOpt.isEmpty()) {
            log.info("[SHIPROCKET] no seller_id on order items; pickup={} (config default)", pickupLocation);
            return pickupLocation;
        }

        Optional<Seller> sellerOpt = sellerRepository.findById(sellerIdOpt.get());
        if (sellerOpt.isEmpty()) {
            log.warn("[SHIPROCKET] seller id={} not found; pickup={} (config default)", sellerIdOpt.get(), pickupLocation);
            return pickupLocation;
        }

        Seller s = sellerOpt.get();
        String mappedPickup = resolveConfiguredPickupForSeller(s);
        if (mappedPickup != null) {
            log.info("[SHIPROCKET] pickup from config mapping sellerId={} pickup={}", s.getId(), mappedPickup);
            return mappedPickup;
        }

        String matchedPickup = resolvePickupFromShiprocketList(token, s);
        if (matchedPickup != null) {
            return matchedPickup;
        }

        log.warn("[SHIPROCKET] seller id={} has no valid pickup match; using default pickup={}", s.getId(), pickupLocation);
        return pickupLocation;
    }

    private String resolveConfiguredPickupForSeller(Seller seller) {
        if (pickupLocationBySellerRaw == null || pickupLocationBySellerRaw.isBlank()) {
            return null;
        }
        String sellerIdKey = String.valueOf(seller.getId());
        for (String entry : pickupLocationBySellerRaw.split(",")) {
            String token = entry == null ? "" : entry.trim();
            if (token.isEmpty()) {
                continue;
            }
            int idx = token.indexOf(':');
            if (idx <= 0 || idx >= token.length() - 1) {
                continue;
            }
            String key = token.substring(0, idx).trim();
            String value = token.substring(idx + 1).trim();
            if (value.isEmpty()) {
                continue;
            }
            if (sellerIdKey.equals(key)) {
                return value;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private String resolvePickupFromShiprocketList(String token, Seller seller) {
        try {
            String url = apiBaseUrl + "/v1/external/settings/company/pickup";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<Void> req = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, req, Map.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return null;
            }

            Object pickupsRaw = pickFirst(response.getBody(), "data.data", "data");
            if (!(pickupsRaw instanceof List<?> pickups) || pickups.isEmpty()) {
                return null;
            }

            String sellerEmail = safeLower(seller.getEmail());
            String sellerPhone = normalizePhone(seller.getMobileNumber());
            String sellerName = safeLower((nz(seller.getFirstName(), "") + " " + nz(seller.getLastName(), "")).trim());
            String business = safeLower(seller.getBusinessName());
            String branch = safeLower(seller.getBranchName());

            for (Object row : pickups) {
                if (!(row instanceof Map<?, ?> m)) {
                    continue;
                }
                String pickup = stringOrNull(m.get("pickup_location"));
                if (pickup == null) {
                    continue;
                }
                String pEmail = safeLower(stringOrNull(m.get("email")));
                String pPhone = normalizePhone(stringOrNull(m.get("phone")));
                String pSellerName = safeLower(stringOrNull(m.get("seller_name")));
                String pPickup = safeLower(pickup);

                if (sellerEmail != null && sellerEmail.equals(pEmail)) {
                    log.info("[SHIPROCKET] pickup matched by email sellerId={} pickup={}", seller.getId(), pickup);
                    return pickup;
                }
                if (sellerPhone != null && sellerPhone.equals(pPhone)) {
                    log.info("[SHIPROCKET] pickup matched by phone sellerId={} pickup={}", seller.getId(), pickup);
                    return pickup;
                }
                if (sellerName != null && pSellerName != null && pSellerName.contains(sellerName)) {
                    log.info("[SHIPROCKET] pickup matched by seller_name sellerId={} pickup={}", seller.getId(), pickup);
                    return pickup;
                }
                if (business != null && business.equals(pPickup)) {
                    log.info("[SHIPROCKET] pickup matched by business_name sellerId={} pickup={}", seller.getId(), pickup);
                    return pickup;
                }
                if (branch != null && branch.equals(pPickup)) {
                    log.info("[SHIPROCKET] pickup matched by branch_name sellerId={} pickup={}", seller.getId(), pickup);
                    return pickup;
                }
            }
        } catch (Exception e) {
            log.warn("[SHIPROCKET] pickup list fetch/match failed sellerId={}: {}", seller.getId(), e.getMessage());
        }
        return null;
    }

    private static Object pickFirst(Map<String, Object> response, String... paths) {
        for (String path : paths) {
            Object value = readPath(response, path);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static Object readPath(Map<String, Object> root, String dottedPath) {
        Object current = root;
        for (String segment : dottedPath.split("\\.")) {
            if (!(current instanceof Map<?, ?> currentMap)) {
                return null;
            }
            current = currentMap.get(segment);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    private static String stringOrNull(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private static String safeLower(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim().toLowerCase();
    }

    private Map<String, Object> buildShipmentPayload(Order order, String pickupName) {
        Map<String, Object> payload = new HashMap<>();

        double subTotal = order.getTotalAmount() == null ? 0 : order.getTotalAmount();
        int subInt = (int) Math.round(subTotal);
        if (subInt < 1) {
            subInt = 1;
        }

        List<Map<String, Object>> orderItems = new ArrayList<>();
        Map<String, Object> line = new HashMap<>();
        line.put("name", "Order " + order.getOrderNumber());
        line.put("sku", "SKU-" + order.getId());
        line.put("units", 1);
        line.put("selling_price", subInt);
        line.put("discount", "");
        line.put("tax", "");
        orderItems.add(line);

        String phone = normalizePhone(order.getShippingPhone());
        String billingEmail = order.getShippingEmail() != null && !order.getShippingEmail().isBlank()
                ? order.getShippingEmail().trim()
                : "orders+" + order.getOrderNumber() + "@noreply.local";

        payload.put("order_id", order.getOrderNumber());
        payload.put("order_date", LocalDate.now().toString());
        payload.put("pickup_location", pickupName);
        payload.put("comment", "Paid order — Shiprocket adhoc");

        payload.put("billing_customer_name", nz(order.getShippingName(), "Customer"));
        payload.put("billing_last_name", "");
        payload.put("billing_address", nz(order.getShippingAddress1(), "-"));
        payload.put("billing_city", nz(order.getShippingCity(), "-"));
        payload.put("billing_pincode", nz(order.getShippingPincode(), "000000"));
        payload.put("billing_state", nz(order.getShippingState(), "-"));
        payload.put("billing_country", "India");
        payload.put("billing_email", billingEmail);
        payload.put("billing_phone", phone);

        payload.put("shipping_is_billing", true);
        payload.put("shipping_customer_name", nz(order.getShippingName(), "Customer"));
        payload.put("shipping_phone", phone);
        payload.put("shipping_address", nz(order.getShippingAddress1(), "-"));
        payload.put("shipping_city", nz(order.getShippingCity(), "-"));
        payload.put("shipping_pincode", nz(order.getShippingPincode(), "000000"));
        payload.put("shipping_state", nz(order.getShippingState(), "-"));
        payload.put("shipping_country", "India");

        payload.put("order_items", orderItems);
        payload.put("payment_method", "Prepaid");
        payload.put("sub_total", subInt);
        payload.put("shipping_charges", order.getShippingAmount() == null ? 0 : (int) Math.round(order.getShippingAmount()));
        payload.put("giftwrap_charges", 0);
        payload.put("transaction_charges", 0);
        payload.put("total_discount", order.getDiscountAmount() == null ? 0 : (int) Math.round(order.getDiscountAmount()));
        payload.put("length", 10);
        payload.put("breadth", 10);
        payload.put("height", 10);
        payload.put("weight", 0.5);

        log.debug("[SHIPROCKET] adhoc payload order_id={} sub_total={} pickup={}", order.getOrderNumber(), subInt, pickupName);
        return payload;
    }

    private static String nz(String v, String d) {
        if (v == null || v.isBlank()) {
            return d;
        }
        return v.length() > 180 ? v.substring(0, 180) : v;
    }

    private static String normalizePhone(String raw) {
        if (raw == null) {
            return "9999999999";
        }
        String digits = raw.replaceAll("\\D", "");
        if (digits.length() >= 10) {
            return digits.substring(digits.length() - 10);
        }
        return (digits + "9999999999").substring(0, 10);
    }

    public String trackShipment(String awb) {
        log.info("[SHIPROCKET] trackShipment AWB={}", awb);
        try {
            String t = getToken();
            String url = apiBaseUrl + "/v1/external/courier/track/awb/" + awb;

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(t);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("[SHIPROCKET] trackShipment OK awb={}", awb);
                return response.getBody().toString();
            }
            log.warn("[SHIPROCKET] trackShipment empty awb={}", awb);
            throw new RuntimeException("Failed to track shipment");

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("[SHIPROCKET] track HTTP {} awb={} body={}", e.getStatusCode(), awb, e.getResponseBodyAsString());
            throw new RuntimeException("Failed to track shipment", e);
        } catch (Exception e) {
            log.error("[SHIPROCKET] track error awb={}: {}", awb, e.getMessage(), e);
            throw new RuntimeException("Failed to track shipment", e);
        }
    }

    public void handleWebhook(Map<String, Object> webhookData) {
        log.info("[SHIPROCKET:WEBHOOK] handleWebhook keys={}", webhookData != null ? webhookData.keySet() : "null");

        try {
            if (webhookData != null && webhookData.containsKey("awb") && webhookData.containsKey("status")) {
                String awb = webhookData.get("awb").toString();
                String status = webhookData.get("status").toString();
                orderService.updateOrderStatusFromWebhook(awb, status);
                log.info("[SHIPROCKET:WEBHOOK] processed awb={} status={}", awb, status);
            } else {
                log.warn("[SHIPROCKET:WEBHOOK] ignored (need awb + status fields)");
            }
        } catch (Exception e) {
            log.error("[SHIPROCKET:WEBHOOK] error: {}", e.getMessage(), e);
        }
    }
}
