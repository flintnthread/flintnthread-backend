package com.ecommerce.authdemo.service.impl;

import com.ecommerce.authdemo.dto.AddressRequest;
import com.ecommerce.authdemo.entity.Address;
import com.ecommerce.authdemo.repository.AddressRepository;
import com.ecommerce.authdemo.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;

    @Override
    public Address addAddress(AddressRequest request) {

        Address address = Address.builder()
                .userId(request.getUserId())
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .pincode(request.getPincode())
                .addressType(request.getAddressType())
                .isDefault(request.getIsDefault())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return addressRepository.save(address);
    }

    @Override
    public List<Address> getUserAddresses(Integer userId) {
        return addressRepository.findByUserId(userId);
    }

    @Override
    public Address updateAddress(Integer id, AddressRequest request) {

        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));

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
        address.setUpdatedAt(LocalDateTime.now());

        return addressRepository.save(address);
    }

    @Override
    public void deleteAddress(Integer id) {
        addressRepository.deleteById(id);
    }

    @Override
    public Address setDefaultAddress(Integer userId, Integer addressId) {

        List<Address> addresses = addressRepository.findByUserId(userId);

        for (Address a : addresses) {
            a.setIsDefault(false);
        }

        Address selected = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        selected.setIsDefault(true);

        addressRepository.saveAll(addresses);
        return addressRepository.save(selected);
    }

    @Override
    public Address getDefaultAddress(Integer userId) {
        return addressRepository.findByUserIdAndIsDefaultTrue(userId)
                .orElseThrow(() -> new RuntimeException("Default address not found"));
    }
}