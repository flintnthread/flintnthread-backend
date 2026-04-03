package com.ecommerce.authdemo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http

                // Disable CSRF for REST APIs
                .csrf(csrf -> csrf.disable())

                // JWT uses stateless session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // --------------------------------
                        // PUBLIC AUTH APIs
                        // --------------------------------
                        .requestMatchers(
                                "/auth/send-otp",
                                "/auth/verify-otp",
                                "/api/auth/**"
                        ).permitAll()

                        // --------------------------------
                        // PUBLIC LOCATION APIs
                        // --------------------------------
                        .requestMatchers("/api/location/**")
                        .permitAll()

                        // --------------------------------
                        // PUBLIC CATEGORY APIs
                        // --------------------------------
                        .requestMatchers(
                                "/api/categories/main",
                                "/api/categories/tree",
                                "/api/categories/*/subcategories",
                                "/api/categories/{id}",
                                "/api/categories/**"
                        ).permitAll()

                        // --------------------------------
                        // PUBLIC PRODUCT APIs
                        // --------------------------------
                        .requestMatchers("/api/products/**")
                        .permitAll()

                        // --------------------------------
                        // PUBLIC SEARCH APIs  ⭐ ADDED
                        // --------------------------------
                        .requestMatchers("/api/search/**")
                        .permitAll()

                        // --------------------------------
                        // ADMIN CATEGORY MANAGEMENT
                        // --------------------------------
                        .requestMatchers(
                                "/api/categories",
                                "/api/categories/create",
                                "/api/categories/update/**",
                                "/api/categories/delete/**"
                        ).hasRole("ADMIN")

                        // --------------------------------
                        // ALL OTHER APIs REQUIRE LOGIN
                        // --------------------------------
                        .anyRequest().authenticated()
                )

                // Add JWT filter
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    // Authentication Manager
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {

        return config.getAuthenticationManager();
    }
}