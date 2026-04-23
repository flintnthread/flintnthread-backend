package com.ecommerce.authdemo.service;

import com.ecommerce.authdemo.dto.ReturnImageRequest;
import com.ecommerce.authdemo.dto.ReturnImageResponse;

import java.util.List;

public interface ReturnImageService {
    ReturnImageResponse create(ReturnImageRequest request);

    List<ReturnImageResponse> getByReturnId(Integer returnId);

    void delete(Integer id);
}
