package com.ecommerce.authdemo.security;


import com.ecommerce.authdemo.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

    @Component
    public class JwtUtil {

        private static final String SECRET =
                "mysecretkeymysecretkeymysecretkeymysecretkey12345";

        private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

        private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 hour


        // Generate JWT Token
        public String generateToken(User user) {

            String identifier;

            if (user.getEmail() != null && !user.getEmail().isBlank()) {
                identifier = user.getEmail();
            } else {
                identifier = user.getContactNumber();
            }

            return Jwts.builder()
                    .setSubject(identifier)
                    .claim("role", user.getRole().name())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();
        }


        // Extract all claims
        public Claims extractClaims(String token) {

            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        }


        // Extract email or mobile
        public String extractIdentifier(String token) {
            return extractClaims(token).getSubject();
        }


        // Extract role
        public String extractRole(String token) {
            return extractClaims(token).get("role", String.class);
        }


        // Check expiration
        public boolean isTokenExpired(String token) {

            Date expiry = extractClaims(token).getExpiration();

            return expiry.before(new Date());
        }


        // Validate token
        public boolean validateToken(String token, String identifier) {

            String tokenIdentifier = extractIdentifier(token);

            return (tokenIdentifier.equals(identifier) && !isTokenExpired(token));
        }
    }

