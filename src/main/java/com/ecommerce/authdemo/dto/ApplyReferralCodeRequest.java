package com.ecommerce.authdemo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApplyReferralCodeRequest {

    @NotBlank(message = "Referral code is required")
    private String referralCode;
}

