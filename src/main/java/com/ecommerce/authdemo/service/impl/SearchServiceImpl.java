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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
    public class SearchServiceImpl implements SearchService {

        @Autowired
        private CategoryRepository categoryRepository;

        @Autowired
        private SubCategoryRepository subCategoryRepository;

        @Autowired
        private ProductRepository productRepository;

        @Override
        public ApiResponse<SearchResponseDTO> search(String keyword) {

            List<Category> categories =
                    categoryRepository.findByCategoryNameContainingIgnoreCase(keyword);

            List<SubCategory> subCategories =
                    subCategoryRepository.findBySubcategoryNameContainingIgnoreCase(keyword);

            List<Product> products =
                    productRepository.findByProductNameContainingIgnoreCase(keyword);

            SearchResponseDTO response =
                    new SearchResponseDTO(categories, subCategories, products);

            return new ApiResponse<>(true, "Search Results", response);
        }
    }

