package com.ecommerce.authdemo.dto;

import lombok.Data;

@Data
public class AddressRequest {

    private Integer userId;

    private String name;
    private String email;
    private String phone;

    private String addressLine1;
    private String addressLine2;

    private String city;
    private String state;
    private String country;

    private String pincode;

    private String addressType;

    private Boolean isDefault;
}