package com.ecommerce.authdemo.controller;

import com.ecommerce.authdemo.dto.UpdateProfileRequest;
import com.ecommerce.authdemo.entity.User;
import com.ecommerce.authdemo.service.AccountService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;


    /*
     ---------------------------------------
     GET USER PROFILE
     ---------------------------------------
     */
    @GetMapping("/{userId}")
    public ResponseEntity<User> getProfile(@PathVariable Long userId) {

        User user = accountService.getProfile(userId);

        return ResponseEntity.ok(user);
    }


    /*
     ---------------------------------------
     UPDATE USER PROFILE
     ---------------------------------------
     */
    @PutMapping("/{userId}")
    public ResponseEntity<User> updateProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {

        User updatedUser = accountService.updateProfile(
                userId,
                request.getUsername(),
                request.getEmail()
        );

        return ResponseEntity.ok(updatedUser);
    }


    /*
     ---------------------------------------
     UPDATE CONTACT NUMBER
     ---------------------------------------
     */
    @PutMapping("/{userId}/contact")
    public ResponseEntity<User> updateContactNumber(
            @PathVariable Long userId,
            @RequestParam String contactNumber) {

        User updatedUser =
                accountService.updateContactNumber(userId, contactNumber);

        return ResponseEntity.ok(updatedUser);
    }


    /*
     ---------------------------------------
     DEACTIVATE ACCOUNT
     ---------------------------------------
     */
    @PutMapping("/{userId}/deactivate")
    public ResponseEntity<String> deactivateAccount(
            @PathVariable Long userId) {

        accountService.deactivateAccount(userId);

        return ResponseEntity.ok("Account deactivated successfully");
    }


    /*
     ---------------------------------------
     DELETE ACCOUNT
     ---------------------------------------
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteAccount(
            @PathVariable Long userId) {

        accountService.deleteAccount(userId);

        return ResponseEntity.ok("Account deleted successfully");
    }

}