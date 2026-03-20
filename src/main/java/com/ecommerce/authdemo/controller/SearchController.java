package com.ecommerce.authdemo.controller;

import com.ecommerce.authdemo.dto.SearchResponseDTO;
import com.ecommerce.authdemo.payload.ApiResponse;
import com.ecommerce.authdemo.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
    @RequestMapping("/api/search")
    public class SearchController {

        @Autowired
        private SearchService searchService;

        @GetMapping
        public ApiResponse<SearchResponseDTO> search(@RequestParam String keyword) {
            return searchService.search(keyword);
        }
    }

