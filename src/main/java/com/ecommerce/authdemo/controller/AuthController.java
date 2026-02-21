package com.ecommerce.authdemo.controller;

import com.ecommerce.authdemo.dto.SellerLoginDTO;
import com.ecommerce.authdemo.dto.SellerRegisterDTO;
import com.ecommerce.authdemo.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SellerService sellerService;

    @PostMapping("/register")
    public String register(@RequestBody SellerRegisterDTO registerDTO) {
        return sellerService.registerSeller(registerDTO);
    }

    @PostMapping("/login")
    public String login(@RequestBody SellerLoginDTO loginDTO) {
        return sellerService.loginSeller(
                loginDTO.getEmail(),
                loginDTO.getPassword()
        );
    }
}
