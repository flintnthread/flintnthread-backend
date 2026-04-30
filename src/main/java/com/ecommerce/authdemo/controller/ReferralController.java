package com.ecommerce.authdemo.controller;

import com.ecommerce.authdemo.dto.ApiResponse;
import com.ecommerce.authdemo.dto.ApplyReferralCodeRequest;
import com.ecommerce.authdemo.dto.ReferralOverviewDTO;
import com.ecommerce.authdemo.exception.OrderException;
import com.ecommerce.authdemo.service.ReferralService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/referrals")
@RequiredArgsConstructor
@Slf4j
public class ReferralController {

    private final ReferralService referralService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ReferralOverviewDTO>> getMyReferralOverview() {
        try {
            ReferralOverviewDTO data = referralService.getMyReferralOverview();
            return ResponseEntity.ok(new ApiResponse<>(true, "Referral overview fetched", data));
        } catch (Exception e) {
            log.error("Failed to fetch referral overview", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, e.getMessage() != null ? e.getMessage() : "Failed to fetch referral overview", null));
        }
    }

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<ReferralOverviewDTO>> applyReferralCode(
            @Valid @RequestBody ApplyReferralCodeRequest request
    ) {
        try {
            ReferralOverviewDTO data = referralService.applyReferralCode(request.getReferralCode());
            return ResponseEntity.ok(new ApiResponse<>(true, "Referral code applied successfully", data));
        } catch (OrderException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Failed to apply referral code", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, e.getMessage() != null ? e.getMessage() : "Failed to apply referral code", null));
        }
    }
}

