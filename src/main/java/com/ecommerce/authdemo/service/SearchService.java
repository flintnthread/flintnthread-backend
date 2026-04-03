package com.ecommerce.authdemo.service;

import com.ecommerce.authdemo.dto.SearchResponseDTO;
import com.ecommerce.authdemo.payload.ApiResponse;

import org.springframework.data.domain.Page;

import java.util.List;

public interface SearchService {

    /*
    -----------------------------------------
    🔎 MAIN SEARCH
    -----------------------------------------
    */
    ApiResponse<SearchResponseDTO> search(String keyword);

    /*
    -----------------------------------------
    🔍 SEARCH SUGGESTIONS
    (Auto complete search bar)
    -----------------------------------------
    */
    ApiResponse<List<String>> getSuggestions(String keyword);

    /*
    -----------------------------------------
    🔥 TRENDING SEARCHES
    -----------------------------------------
    */
    ApiResponse<List<String>> getTrendingSearches();

    /*
    -----------------------------------------
    📄 PAGINATED SEARCH
    -----------------------------------------
    */
    ApiResponse<Page<SearchResponseDTO>> searchWithPagination(
            String keyword,
            int page,
            int size
    );

    /*
    -----------------------------------------
    🎤 VOICE SEARCH
    -----------------------------------------
    */
    ApiResponse<SearchResponseDTO> voiceSearch(String keyword);

}