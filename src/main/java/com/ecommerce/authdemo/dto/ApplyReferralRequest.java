package com.ecommerce.authdemo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplyReferralRequest {

    private Long userId;
    private String referralCode;
}