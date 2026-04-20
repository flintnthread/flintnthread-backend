package com.ecommerce.authdemo.util;

import com.ecommerce.authdemo.entity.User;
import com.ecommerce.authdemo.exception.ResourceNotFoundException;
import com.ecommerce.authdemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final UserRepository userRepository;

    public User getCurrentUser() {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {

            throw new RuntimeException("User not authenticated");
        }

        String principal = authentication.getName();

        return userRepository.findByEmail(principal)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with email: " + principal)
                );
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}