package com.ecommerce.authdemo.service.impl;

import com.ecommerce.authdemo.dto.SearchResponseDTO;
import com.ecommerce.authdemo.entity.Category;
import com.ecommerce.authdemo.entity.Product;
import com.ecommerce.authdemo.entity.SearchHistory;
import com.ecommerce.authdemo.entity.SubCategory;
import com.ecommerce.authdemo.entity.User;
import com.ecommerce.authdemo.payload.ApiResponse;
import com.ecommerce.authdemo.repository.CategoryRepository;
import com.ecommerce.authdemo.repository.ProductRepository;
import com.ecommerce.authdemo.repository.SearchHistoryRepository;
import com.ecommerce.authdemo.repository.SubCategoryRepository;
import com.ecommerce.authdemo.service.SearchService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private static final Logger log = LoggerFactory.getLogger(SearchServiceImpl.class);

    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final ProductRepository productRepository;
    private final SearchHistoryRepository searchHistoryRepository;

    /*
    -----------------------------------------
    🔎 MAIN SEARCH
    -----------------------------------------
    */
    @Override
    @Transactional
    public ApiResponse<SearchResponseDTO> search(String keyword, Long userId, String sessionId) {

        if (!StringUtils.hasText(keyword)) {
            return new ApiResponse<>(false, "Search keyword cannot be empty", null);
        }

        keyword = keyword.trim();
        saveSearchHistory(keyword, userId, sessionId);

        List<Category> categories =
                categoryRepository.findTop10ByCategoryNameContainingIgnoreCase(keyword);

        List<SubCategory> subCategories =
                subCategoryRepository.findTop10BySubcategoryNameContainingIgnoreCase(keyword);

        List<Product> products =
                productRepository.findTop20ByNameContainingIgnoreCaseAndStatus(keyword, "active");

        SearchResponseDTO response =
                new SearchResponseDTO(categories, subCategories, products);

        return new ApiResponse<>(true, "Search results fetched successfully", response);
    }

    /*
    -----------------------------------------
    🔍 SEARCH SUGGESTIONS
    -----------------------------------------
    */
    @Override
    public ApiResponse<List<String>> getSuggestions(String keyword) {

        if (!StringUtils.hasText(keyword)) {
            return new ApiResponse<>(false, "Keyword required", null);
        }

        keyword = keyword.trim();

        List<String> suggestions =
                productRepository.findTop5ByNameContainingIgnoreCase(keyword)
                        .stream()
                        .map(Product::getName)
                        .distinct()
                        .collect(Collectors.toList());

        return new ApiResponse<>(true, "Suggestions fetched successfully", suggestions);
    }

    /*
    -----------------------------------------
    🔥 TRENDING SEARCHES
    -----------------------------------------
    */
    @Override
    public ApiResponse<List<String>> getTrendingSearches() {

        List<String> trending =
                productRepository.findTop10ByOrderByIdDesc()
                        .stream()
                        .map(Product::getName)
                        .collect(Collectors.toList());

        return new ApiResponse<>(true, "Trending searches fetched", trending);
    }

    /*
    -----------------------------------------
    📄 SEARCH WITH PAGINATION
    -----------------------------------------
    */
    @Override
    @Transactional
    public ApiResponse<Page<SearchResponseDTO>> searchWithPagination(
            String keyword,
            Long userId,
            String sessionId,
            int page,
            int size) {

        if (!StringUtils.hasText(keyword)) {
            return new ApiResponse<>(false, "Search keyword cannot be empty", null);
        }

        keyword = keyword.trim();
        saveSearchHistory(keyword, userId, sessionId);

        Pageable pageable =
                PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Product> productPage =
                productRepository.findByNameContainingIgnoreCaseAndStatus(
                        keyword,
                        "active",
                        pageable
                );

        Page<SearchResponseDTO> responsePage =
                productPage.map(product ->
                        new SearchResponseDTO(
                                Collections.emptyList(),
                                Collections.emptyList(),
                                List.of(product)
                        )
                );

        return new ApiResponse<>(true, "Search results fetched successfully", responsePage);
    }

    /*
    -----------------------------------------
    🎤 VOICE SEARCH
    -----------------------------------------
    */
    @Override
    @Transactional
    public ApiResponse<SearchResponseDTO> voiceSearch(String keyword, Long userId, String sessionId) {

        if (!StringUtils.hasText(keyword)) {
            return new ApiResponse<>(false, "Voice keyword required", null);
        }

        keyword = keyword.trim();
        saveSearchHistory(keyword, userId, sessionId);

        List<Product> products =
                productRepository.findTop20ByNameContainingIgnoreCaseAndStatus(
                        keyword,
                        "active"
                );

        if (products.isEmpty()) {
            return new ApiResponse<>(false, "No product found", null);
        }

        SearchResponseDTO response =
                new SearchResponseDTO(
                        Collections.emptyList(),
                        Collections.emptyList(),
                        products
                );

        return new ApiResponse<>(true, "Voice search result", response);
    }

    @Override
    public ApiResponse<List<String>> getSearchHistory(Long userId, String sessionId) {
        if (userId == null && !StringUtils.hasText(sessionId)) {
            return new ApiResponse<>(false, "userId or sessionId is required", List.of());
        }

        List<SearchHistory> entries = userId != null
                ? searchHistoryRepository.findTop20ByUserIdOrderBySearchedAtDesc(userId)
                : searchHistoryRepository.findTop20BySessionIdOrderBySearchedAtDesc(sessionId.trim());

        LinkedHashSet<String> uniqueKeywords = new LinkedHashSet<>();
        for (SearchHistory entry : entries) {
            if (StringUtils.hasText(entry.getKeyword())) {
                uniqueKeywords.add(entry.getKeyword().trim());
            }
        }

        return new ApiResponse<>(
                true,
                "Search history fetched successfully",
                new ArrayList<>(uniqueKeywords)
        );
    }

    @Override
    @Transactional
    public ApiResponse<String> clearSearchHistory(Long userId, String sessionId) {
        if (userId == null && !StringUtils.hasText(sessionId)) {
            return new ApiResponse<>(false, "userId or sessionId is required", null);
        }

        if (userId != null) {
            searchHistoryRepository.deleteByUserId(userId);
        } else {
            searchHistoryRepository.deleteBySessionId(sessionId.trim());
        }

        return new ApiResponse<>(true, "Search history cleared successfully", "CLEARED");
    }

    private void saveSearchHistory(String keyword, Long userId, String sessionId) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }

        SearchHistory history = new SearchHistory();
        history.setKeyword(keyword.trim());

        if (userId != null) {
            User user = new User();
            user.setId(userId);
            history.setUser(user);
        } else if (StringUtils.hasText(sessionId)) {
            history.setSessionId(sessionId.trim());
        } else {
            return;
        }

        try {
            searchHistoryRepository.save(history);
        } catch (org.springframework.dao.DataAccessException ex) {
            log.warn("Skipping search history persistence: {}", ex.getMostSpecificCause() != null
                    ? ex.getMostSpecificCause().getMessage()
                    : ex.getMessage());
        }
    }
}