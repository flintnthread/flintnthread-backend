package com.ecommerce.authdemo.service.impl;

import com.ecommerce.authdemo.dto.ExchangeImageRequest;
import com.ecommerce.authdemo.dto.ExchangeImageResponse;
import com.ecommerce.authdemo.entity.ExchangeImage;
import com.ecommerce.authdemo.exception.ResourceNotFoundException;
import com.ecommerce.authdemo.repository.ExchangeImageRepository;
import com.ecommerce.authdemo.service.ExchangeImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExchangeImageServiceImpl implements ExchangeImageService {

    private final ExchangeImageRepository exchangeImageRepository;

    @Override
    public ExchangeImageResponse create(ExchangeImageRequest request) {
        ExchangeImage entity = ExchangeImage.builder()
                .exchangeId(request.getExchangeId())
                .imagePath(request.getImagePath().trim())
                .build();
        return toResponse(exchangeImageRepository.save(entity));
    }

    @Override
    public List<ExchangeImageResponse> getByExchangeId(Integer exchangeId) {
        return exchangeImageRepository.findByExchangeIdOrderByCreatedAtDesc(exchangeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void delete(Integer id) {
        ExchangeImage entity = exchangeImageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exchange image not found"));
        exchangeImageRepository.delete(entity);
    }

    private ExchangeImageResponse toResponse(ExchangeImage entity) {
        return ExchangeImageResponse.builder()
                .id(entity.getId())
                .exchangeId(entity.getExchangeId())
                .imagePath(entity.getImagePath())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
