package com.ecommerce.authdemo.service;

import com.ecommerce.authdemo.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ShiprocketService {

    private static final Logger logger = LoggerFactory.getLogger(ShiprocketService.class);
    
    private final RestTemplate restTemplate;
    private final OrderService orderService;

    @Value("${shiprocket.email}")
    private String email;

    @Value("${shiprocket.password}")
    private String password;

    @Value("${shiprocket.api.base-url:https://apiv2.shiprocket.in}")
    private String apiBaseUrl;

    public String getToken() {
        try {
            String url = apiBaseUrl + "/v1/external/auth/login";

            Map<String, String> body = Map.of(
                    "email", email,
                    "password", password
            );

            ResponseEntity<Map> response = restTemplate.postForEntity(url, body, Map.class);
            
            if (response.getBody() != null && response.getBody().containsKey("token")) {
                String token = (String) response.getBody().get("token");
                logger.info("Successfully obtained Shiprocket token");
                return token;
            } else {
                throw new RuntimeException("Failed to obtain token from Shiprocket API");
            }
        } catch (Exception e) {
            logger.error("Error getting Shiprocket token: {}", e.getMessage());
            throw new RuntimeException("Failed to authenticate with Shiprocket", e);
        }
    }

    public void createShipment(Order order) {
        try {
            logger.info("Creating shipment for order: {}", order.getOrderNumber());
            
            String token = getToken();
            String url = apiBaseUrl + "/v1/external/orders/create/adhoc";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> payload = buildShipmentPayload(order);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                
                if (responseBody.containsKey("shipment_id")) {
                    String shipmentId = responseBody.get("shipment_id").toString();
                    String awb = responseBody.containsKey("awb_code") ? 
                        responseBody.get("awb_code").toString() : 
                        "SR_" + shipmentId;
                    
                    String trackingUrl = responseBody.containsKey("tracking_url") ?
                        responseBody.get("tracking_url").toString() :
                        "https://shiprocket.in/tracking/" + awb;

                    logger.info("Shipment created successfully - AWB: {}", awb);
                    orderService.updateShipment(order.getOrderNumber(), awb, "Shiprocket", trackingUrl);
                } else {
                    throw new RuntimeException("Invalid response from Shiprocket API");
                }
            } else {
                throw new RuntimeException("Failed to create shipment with Shiprocket");
            }

        } catch (Exception e) {
            logger.error("Error creating shipment for order {}: {}", order.getOrderNumber(), e.getMessage());
            
            // Fallback to temporary tracking for development
            String awb = "TEMP_AWB_" + System.currentTimeMillis();
            String tracking = "https://shiprocket.in/tracking/" + awb;
            orderService.updateShipment(order.getOrderNumber(), awb, "Shiprocket", tracking);
            
            throw new RuntimeException("Shipment creation failed, using fallback tracking", e);
        }
    }

    private Map<String, Object> buildShipmentPayload(Order order) {
        Map<String, Object> payload = new HashMap<>();

        // Order details
        payload.put("order_id", order.getOrderNumber());
        payload.put("order_date", LocalDate.now().toString());
        payload.put("pickup_location", "Primary");
        payload.put("comment", "Order created via e-commerce platform");

        // Billing details (using shipping info as billing)
        payload.put("billing_customer_name", order.getShippingName());
        payload.put("billing_phone", order.getShippingPhone());
        payload.put("billing_address", order.getShippingAddress1());
        payload.put("billing_city", order.getShippingCity());
        payload.put("billing_pincode", order.getShippingPincode());
        payload.put("billing_state", order.getShippingState());
        payload.put("billing_country", "India");

        // Shipping details (same as billing for now)
        payload.put("shipping_customer_name", order.getShippingName());
        payload.put("shipping_phone", order.getShippingPhone());
        payload.put("shipping_address", order.getShippingAddress1());
        payload.put("shipping_city", order.getShippingCity());
        payload.put("shipping_pincode", order.getShippingPincode());
        payload.put("shipping_state", order.getShippingState());
        payload.put("shipping_country", "India");

        // Payment and order details
        payload.put("payment_method", "Prepaid");
        payload.put("sub_total", order.getTotalAmount());
        payload.put("length", 10);
        payload.put("breadth", 10);
        payload.put("height", 10);
        payload.put("weight", 0.5);

        return payload;
    }

    public String trackShipment(String awb) {
        try {
            String token = getToken();
            String url = apiBaseUrl + "/v1/external/courier/track/awb/" + awb;

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().toString();
            } else {
                throw new RuntimeException("Failed to track shipment");
            }

        } catch (Exception e) {
            logger.error("Error tracking shipment AWB {}: {}", awb, e.getMessage());
            throw new RuntimeException("Failed to track shipment", e);
        }
    }

    public void handleWebhook(Map<String, Object> webhookData) {
        try {
            logger.info("Processing Shiprocket webhook: {}", webhookData);
            
            if (webhookData.containsKey("awb") && webhookData.containsKey("status")) {
                String awb = webhookData.get("awb").toString();
                String status = webhookData.get("status").toString();
                
                // Update order status based on webhook
                orderService.updateOrderStatusFromWebhook(awb, status);
                
                logger.info("Updated shipment status for AWB {}: {}", awb, status);
            }
            
        } catch (Exception e) {
            logger.error("Error processing webhook: {}", e.getMessage());
        }
    }
}
