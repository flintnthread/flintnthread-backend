package com.ecommerce.authdemo.dto;

import lombok.Data;

@Data
public class SellerRegisterDTO {

        private String firstName;
        private String lastName;
        private String mobileNumber;
        private String email;
        private String password;
        private String confirmPassword;
    }


