package com.ecommerce.authdemo.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public class ReferralDashboardDto {

        private String referralCode;
        private long completedInvites;
        private int targetInvites;
        private long remainingInvites;
        private boolean eligible;
        private String reward;
    }

