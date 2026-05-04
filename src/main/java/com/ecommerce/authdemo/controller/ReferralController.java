package com.ecommerce.authdemo.controller;

import com.ecommerce.authdemo.dto.ApplyReferralRequest;
import com.ecommerce.authdemo.dto.ReferralDashboardDto;
import com.ecommerce.authdemo.dto.ReferralResponse;
import com.ecommerce.authdemo.dto.ShareDto;
import com.ecommerce.authdemo.service.ReferralService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/referral")
@RequiredArgsConstructor
public class ReferralController {

    private final ReferralService referralService;

    @PostMapping("/apply")
    public ReferralResponse apply(
            @RequestBody ApplyReferralRequest request) {

        return referralService.applyReferral(
                request.getUserId(),
                request.getReferralCode()
        );
    }

    @GetMapping("/discount/{userId}/{subtotal}")
    public BigDecimal discount(
            @PathVariable Long userId,
            @PathVariable BigDecimal subtotal) {

        return referralService.calculateFirstOrderDiscount(
                userId,
                subtotal
        );
    }

    @GetMapping("/dashboard/{userId}")
    public ReferralDashboardDto dashboard(
            @PathVariable Long userId) {

        return referralService.getDashboard(userId);
    }

    @PostMapping("/refresh/{userId}")
    public String refreshCode(
            @PathVariable Long userId) {

        return referralService.refreshReferralCode(userId);
    }

    @GetMapping("/share/{userId}")
    public ShareDto share(
            @PathVariable Long userId) {

        return referralService.getShareData(userId);
    }

    @PostMapping("/generate/{userId}/{username}")
    public String generate(
            @PathVariable Long userId,
            @PathVariable String username) {

        referralService.generateCodes(userId, username);
        return "Referral code generated successfully";
    }
}