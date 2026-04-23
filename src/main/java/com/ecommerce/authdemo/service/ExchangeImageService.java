package com.ecommerce.authdemo.service;

import com.ecommerce.authdemo.dto.ExchangeImageRequest;
import com.ecommerce.authdemo.dto.ExchangeImageResponse;

import java.util.List;

public interface ExchangeImageService {
    ExchangeImageResponse create(ExchangeImageRequest request);

    List<ExchangeImageResponse> getByExchangeId(Integer exchangeId);

    void delete(Integer id);
}
