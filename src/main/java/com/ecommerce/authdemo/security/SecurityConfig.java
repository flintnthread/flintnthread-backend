
package com.ecommerce.authdemo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.http.HttpMethod;
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
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // --------------------------------
                        // PUBLIC AUTH APIs
                        // --------------------------------
                        .requestMatchers(
                                "/auth/send-otp",
                                "/auth/verify-otp",
                                "/api/auth/**"
                        ).permitAll()


                        .requestMatchers("/image/**")
                        .permitAll()


                        // --------------------------------
                        // PUBLIC LOCATION APIs
                        // --------------------------------
                        .requestMatchers("/api/location/update")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/location/cities")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/location/countries")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/location/states")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/location/pincodes")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/location/countries")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/location/cities")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/location/states")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/location/pincodes")
                        .permitAll()

                                // --------------------------------
                                // CART APIs
                                // --------------------------------
                                .requestMatchers("/api/cart/**")
                                .permitAll()
// --------------------------------
// WISHLIST APIs
// --------------------------------
                                .requestMatchers("/api/wishlist/**")
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
                        // REVIEWS
                        // Public: active list for shoppers. Admin "all" lists stay authenticated.
                        // --------------------------------
                        .requestMatchers(HttpMethod.GET, "/api/reviews/product/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/reviews")
                        .permitAll()

                        // --------------------------------
                        // PUBLIC BANNER APIs
                        // --------------------------------
                        .requestMatchers("/api/banners/**")
                        .permitAll()

                        // --------------------------------
                        // PAYMENT (Razorpay) — Postman / checkout without JWT
                        // --------------------------------
                        .requestMatchers("/api/payment/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/delivery-charges")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/delivery-charges/by-weight")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/delivery-options")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/exchange-images")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/return-images")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/faq-categories")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/faqs")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/invoices")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/invoices/by-number/**")
                        .permitAll()

                        // --------------------------------
                        // SHIPROCKET WEBHOOK (PUBLIC CALLBACK)
                        // --------------------------------
                        .requestMatchers(HttpMethod.POST, "/api/shiprocket/webhook", "/api/shiprocket/webhook/**")
                        .permitAll()

                        // --------------------------------
                        // CONTACT US (PUBLIC SUBMIT)
                        // --------------------------------
                        .requestMatchers("/api/contact/submit")
                        .permitAll()
                        // --------------------------------
                        // SUPPORT TICKETS (AUTHENTICATED CREATE)
                        // --------------------------------
                        .requestMatchers(HttpMethod.POST, "/api/support-tickets")
                        .authenticated()

                        // --------------------------------
                        // COOKIES POLICY (PUBLIC READ)
                        // --------------------------------
                        .requestMatchers("/api/cookies-policy")
                        .permitAll()
                        .requestMatchers("/api/terms-conditions")
                        .permitAll()

                        // --------------------------------
                        // SHIPROCKET SYNC LOGS (ADMIN ONLY)
                        // --------------------------------
                        .requestMatchers("/api/shiprocket/sync-logs/**")
                        .hasRole("ADMIN")

                        // --------------------------------
                        // CONTACT MESSAGES (ADMIN ONLY)
                        // --------------------------------
                        .requestMatchers("/api/contact/messages/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/delivery-charges/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/delivery-options/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/email-logs/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/exchange-images/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/return-images/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/faq-categories/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/faqs/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/invoices/**")
                        .hasRole("ADMIN")

                        // --------------------------------
                        // COOKIES POLICY (ADMIN UPDATE)
                        // --------------------------------
                        .requestMatchers("/api/cookies-policy/admin/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/terms-conditions/admin/**")
                        .hasRole("ADMIN")

                        // --------------------------------
                        // ADMIN LOCATION CITY MANAGEMENT
                        // --------------------------------
                        .requestMatchers(
                                "/api/location/cities",
                                "/api/location/cities/**"
                        ).hasRole("ADMIN")
                        .requestMatchers(
                                "/api/location/countries/**"
                        ).hasRole("ADMIN")
                        .requestMatchers(
                                "/api/location/states/**"
                        ).hasRole("ADMIN")
                        .requestMatchers(
                                "/api/location/pincodes/**"
                        ).hasRole("ADMIN")

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