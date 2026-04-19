package com.ecommerce.authdemo.service.impl;

import com.ecommerce.authdemo.dto.AddressRequest;
import com.ecommerce.authdemo.entity.Address;
import com.ecommerce.authdemo.repository.AddressRepository;
import com.ecommerce.authdemo.service.AddressService;
import com.ecommerce.authdemo.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final SecurityUtil securityUtil;

    private Integer getUserId() {
        return securityUtil.getCurrentUser().getId().intValue();
    }

    @Override
    public Address addAddress(AddressRequest request) {

        Integer userId = getUserId();

        boolean isFirst = addressRepository.findByUserId(userId).isEmpty();

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefault(userId);
        }

        if (request.getLatitude() != null && request.getLongitude() != null) {

            Map<String, String> location =
                    getAddressFromLatLng(request.getLatitude(), request.getLongitude());

            request.setAddressLine1(location.get("fullAddress"));
            request.setCity(location.get("city"));
            request.setState(location.get("state"));
            request.setCountry(location.get("country"));
            request.setPincode(location.get("pincode"));
        }

        if (request.getCity() == null || request.getCity().trim().isEmpty()) {
            throw new IllegalArgumentException("City is required");
        }

        Address address = Address.builder()
                .userId(userId)
                .name(request.getName() != null ? request.getName() : "Current Location")
                .email(
                        request.getEmail() != null
                                ? request.getEmail()
                                : securityUtil.getCurrentUser().getEmail()
                )
                .phone(
                        request.getPhone() != null
                                ? request.getPhone()
                                : securityUtil.getCurrentUser().getContactNumber()
                )
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .pincode(request.getPincode())
                .addressType(request.getAddressType() != null ? request.getAddressType() : "current")
                .isDefault(isFirst || Boolean.TRUE.equals(request.getIsDefault()))
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return addressRepository.save(address);
    }

    private Map<String, String> getAddressFromLatLng(Double lat, Double lng) {

        Map<String, String> result = new HashMap<>();

        try {
            String url = "https://nominatim.openstreetmap.org/reverse?format=json&lat="
                    + lat + "&lon=" + lng;

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "ecommerce-app");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (response.getBody() != null) {

                Map body = response.getBody();

                result.put("fullAddress", body.get("display_name") != null
                        ? body.get("display_name").toString()
                        : "Current Location");

                Map address = (Map) body.get("address");

                if (address != null) {
                    result.put("city", getSafe(address, "city", "town", "village"));
                    result.put("state", getSafe(address, "state"));
                    result.put("country", getSafe(address, "country"));
                    result.put("pincode", getSafe(address, "postcode"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        result.putIfAbsent("fullAddress", "Current Location");
        result.putIfAbsent("city", "Unknown City");
        result.putIfAbsent("state", "Unknown State");
        result.putIfAbsent("country", "India");
        result.putIfAbsent("pincode", "000000");

        return result;
    }

    private String getSafe(Map map, String... keys) {
        for (String key : keys) {
            if (map.get(key) != null) {
                return map.get(key).toString();
            }
        }
        return "Unknown";
    }

    @Override
    public List<Address> getUserAddresses() {
        return addressRepository.findByUserId(getUserId());
    }

    @Override
    public Address updateAddress(Integer id, AddressRequest request) {

        Integer userId = getUserId();

        Address address = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefault(userId);
        }

        address.setName(request.getName());
        address.setEmail(request.getEmail());
        address.setPhone(request.getPhone());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setCountry(request.getCountry());
        address.setPincode(request.getPincode());
        address.setAddressType(request.getAddressType());

        if (request.getIsDefault() != null) {
            address.setIsDefault(request.getIsDefault());
        }

        address.setUpdatedAt(LocalDateTime.now());

        return addressRepository.save(address);
    }

    @Override
    public void deleteAddress(Integer id) {

        Integer userId = getUserId();

        Address address = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));

        addressRepository.delete(address);
    }

    @Override
    public Address setDefaultAddress(Integer addressId) {

        Integer userId = getUserId();

        clearDefault(userId);

        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));

        address.setIsDefault(true);

        return addressRepository.save(address);
    }

    @Override
    public Address getDefaultAddress() {
        return addressRepository.findByUserIdAndIsDefaultTrue(getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Default address not found"));
    }

    private void clearDefault(Integer userId) {
        List<Address> addresses = addressRepository.findByUserId(userId);
        for (Address a : addresses) {
            a.setIsDefault(false);
        }
        addressRepository.saveAll(addresses);
    }
}