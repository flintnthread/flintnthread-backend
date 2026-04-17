package com.ecommerce.authdemo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
    public class UpdateProfileDTO {

        @NotBlank
        private String name;

        private String contactNumber;
    }

