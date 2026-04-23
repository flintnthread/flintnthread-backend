package com.ecommerce.authdemo.service.impl;

import com.ecommerce.authdemo.dto.ReturnImageRequest;
import com.ecommerce.authdemo.dto.ReturnImageResponse;
import com.ecommerce.authdemo.entity.ReturnImage;
import com.ecommerce.authdemo.exception.ResourceNotFoundException;
import com.ecommerce.authdemo.repository.ReturnImageRepository;
import com.ecommerce.authdemo.service.ReturnImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReturnImageServiceImpl implements ReturnImageService {

    private final ReturnImageRepository returnImageRepository;

    @Override
    public ReturnImageResponse create(ReturnImageRequest request) {
        ReturnImage entity = ReturnImage.builder()
                .returnId(request.getReturnId())
                .imagePath(request.getImagePath().trim())
                .build();
        return toResponse(returnImageRepository.save(entity));
    }

    @Override
    public List<ReturnImageResponse> getByReturnId(Integer returnId) {
        return returnImageRepository.findByReturnIdOrderByCreatedAtDesc(returnId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void delete(Integer id) {
        ReturnImage entity = returnImageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return image not found"));
        returnImageRepository.delete(entity);
    }

    private ReturnImageResponse toResponse(ReturnImage entity) {
        return ReturnImageResponse.builder()
                .id(entity.getId())
                .returnId(entity.getReturnId())
                .imagePath(entity.getImagePath())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
