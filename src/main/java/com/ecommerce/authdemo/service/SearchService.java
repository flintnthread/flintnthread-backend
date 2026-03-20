package com.ecommerce.authdemo.service;
import com.ecommerce.authdemo.dto.SearchResponseDTO;
import com.ecommerce.authdemo.payload.ApiResponse;

public interface SearchService {

    ApiResponse<SearchResponseDTO> search(String keyword);

}
