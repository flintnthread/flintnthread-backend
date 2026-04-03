package com.ecommerce.authdemo.service.impl;

import com.ecommerce.authdemo.dto.SearchResponseDTO;
import com.ecommerce.authdemo.entity.Category;
import com.ecommerce.authdemo.entity.Product;
import com.ecommerce.authdemo.entity.SubCategory;
import com.ecommerce.authdemo.payload.ApiResponse;
import com.ecommerce.authdemo.repository.CategoryRepository;
import com.ecommerce.authdemo.repository.ProductRepository;
import com.ecommerce.authdemo.repository.SubCategoryRepository;
import com.ecommerce.authdemo.service.SearchService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final ProductRepository productRepository;

    /*
    -----------------------------------------
    🔎 MAIN SEARCH
    -----------------------------------------
    */
    @Override
    public ApiResponse<SearchResponseDTO> search(String keyword) {

        if (!StringUtils.hasText(keyword)) {
            return new ApiResponse<>(false, "Search keyword cannot be empty", null);
        }

        keyword = keyword.trim();

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
    public ApiResponse<Page<SearchResponseDTO>> searchWithPagination(
            String keyword,
            int page,
            int size) {

        if (!StringUtils.hasText(keyword)) {
            return new ApiResponse<>(false, "Search keyword cannot be empty", null);
        }

        keyword = keyword.trim();

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
    public ApiResponse<SearchResponseDTO> voiceSearch(String keyword) {

        if (!StringUtils.hasText(keyword)) {
            return new ApiResponse<>(false, "Voice keyword required", null);
        }

        keyword = keyword.trim();

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
}