package com.ecommerce.authdemo.service;

import com.ecommerce.authdemo.entity.User;

public interface AccountService {

    User getProfile(Long userId);

    User updateProfile(Long userId, String username, String email);

    void deleteAccount(Long userId);

    User updateContactNumber(Long userId, String contactNumber);

    void deactivateAccount(Long userId);


}