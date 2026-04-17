package com.ecommerce.authdemo.controller;


import com.ecommerce.authdemo.entity.ProductReview;
import com.ecommerce.authdemo.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
    @RequestMapping("/api/reviews")
    @RequiredArgsConstructor
    public class ReviewController {

        private final ReviewRepository reviewRepository;

        @PostMapping
        public ResponseEntity<?> addReview(@RequestBody ProductReview review) {
            return ResponseEntity.ok(reviewRepository.save(review));
        }

        @GetMapping("/product/{productId}")
        public ResponseEntity<?> getReviews(@PathVariable Long productId) {
            return ResponseEntity.ok(reviewRepository.findByProductId(productId));
        }
    }

