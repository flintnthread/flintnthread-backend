package com.ecommerce.authdemo.controller;

import com.ecommerce.authdemo.dto.AddressRequest;
import com.ecommerce.authdemo.entity.Address;
import com.ecommerce.authdemo.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping("/add")
    public Address addAddress(@RequestBody AddressRequest request) {
        return addressService.addAddress(request);
    }

    @GetMapping("/user/{userId}")
    public List<Address> getUserAddresses(@PathVariable Integer userId) {
        return addressService.getUserAddresses(userId);
    }

    @PutMapping("/update/{id}")
    public Address updateAddress(@PathVariable Integer id,
                                 @RequestBody AddressRequest request) {
        return addressService.updateAddress(id, request);
    }

    @DeleteMapping("/delete/{id}")
    public String deleteAddress(@PathVariable Integer id) {
        addressService.deleteAddress(id);
        return "Address Deleted";
    }

    @PutMapping("/default/{userId}/{addressId}")
    public Address setDefaultAddress(@PathVariable Integer userId,
                                     @PathVariable Integer addressId) {
        return addressService.setDefaultAddress(userId, addressId);
    }

    @GetMapping("/default/{userId}")
    public Address getDefaultAddress(@PathVariable Integer userId) {
        return addressService.getDefaultAddress(userId);
    }
}