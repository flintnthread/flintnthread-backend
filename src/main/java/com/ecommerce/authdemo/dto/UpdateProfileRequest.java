package com.ecommerce.authdemo.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {

    private String name;

    private String username;

    private String email;

}