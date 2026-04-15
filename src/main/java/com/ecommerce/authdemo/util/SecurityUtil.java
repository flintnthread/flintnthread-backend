package com.ecommerce.authdemo.util;

import com.ecommerce.authdemo.entity.User;
import com.ecommerce.authdemo.exception.ResourceNotFoundException;
import com.ecommerce.authdemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

    @Component
    @RequiredArgsConstructor
    public class SecurityUtil {

        private final UserRepository userRepository;

        /**
         * Get currently logged-in user using JWT (email/username)
         */
        public User getCurrentUser() {

            String principal = SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getName();

            return userRepository.findByEmail(principal)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("User not found with email: " + principal)
                    );
        }
    }

