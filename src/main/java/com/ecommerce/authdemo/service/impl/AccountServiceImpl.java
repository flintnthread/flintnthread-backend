package com.ecommerce.authdemo.service.impl;

import com.ecommerce.authdemo.dto.Enum.UserStatus;
import com.ecommerce.authdemo.entity.User;
import com.ecommerce.authdemo.repository.UserRepository;
import com.ecommerce.authdemo.service.AccountService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final UserRepository userRepository;

    @Override
    public User getProfile(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    @Override
    @Transactional
    public User updateProfile(Long userId, String username, String email) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Update username
        if (username != null && !username.trim().isEmpty()) {

            if (!username.equals(user.getUsername()) &&
                    userRepository.existsByUsername(username)) {

                throw new RuntimeException("Username already taken");
            }

            user.setUsername(username);
        }

        // Update email
        if (email != null && !email.trim().isEmpty()) {

            if (!email.equals(user.getEmail()) &&
                    userRepository.existsByEmail(email)) {

                throw new RuntimeException("Email already exists");
            }

            user.setEmail(email);
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteAccount(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        userRepository.delete(user);
    }

    @Override
    public User updateContactNumber(Long userId, String contactNumber) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setContactNumber(contactNumber);

        return userRepository.save(user);
    }



    @Override
    public void deactivateAccount(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStatus(UserStatus.valueOf("inactive"));

        userRepository.save(user);
    }
}