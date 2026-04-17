package com.ecommerce.authdemo.controller;

import com.ecommerce.authdemo.dto.UpdateProfileDTO;
import com.ecommerce.authdemo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
    @RequestMapping("/api/user")
    @RequiredArgsConstructor
    public class UserController {

        private final UserService userService;

        @GetMapping("/profile")
        public ResponseEntity<?> getProfile() {
            return ResponseEntity.ok(userService.getProfile());
        }

        @PutMapping("/profile")
        public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateProfileDTO dto) {
            return ResponseEntity.ok(userService.updateProfile(dto));
        }

    @PostMapping("/profile/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(userService.uploadProfileImage(file));
    }
    
        @DeleteMapping
        public ResponseEntity<?> deleteAccount() {
            userService.deleteAccount();
            return ResponseEntity.ok("Account deleted successfully");
        }
    }

