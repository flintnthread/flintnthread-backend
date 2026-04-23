package com.ecommerce.authdemo.service.impl;

import com.ecommerce.authdemo.dto.ContactMessageRequest;
import com.ecommerce.authdemo.dto.ContactMessageResponse;
import com.ecommerce.authdemo.entity.ContactMessage;
import com.ecommerce.authdemo.exception.ResourceNotFoundException;
import com.ecommerce.authdemo.repository.ContactMessageRepository;
import com.ecommerce.authdemo.service.ContactMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactMessageServiceImpl implements ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;

    @Override
    public ContactMessageResponse create(ContactMessageRequest request) {
        ContactMessage entity = ContactMessage.builder()
                .name(request.getName().trim())
                .email(request.getEmail().trim())
                .phone(normalize(request.getPhone()))
                .subject(normalize(request.getSubject()))
                .message(request.getMessage().trim())
                .status(Boolean.FALSE)
                .build();

        ContactMessage saved = contactMessageRepository.save(entity);
        return toResponse(saved);
    }

    @Override
    public List<ContactMessageResponse> getAll(Boolean status) {
        return contactMessageRepository.findAllByStatusFilter(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ContactMessageResponse updateStatus(Integer id, Boolean status) {
        ContactMessage entity = contactMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact message not found"));
        entity.setStatus(status);
        ContactMessage saved = contactMessageRepository.save(entity);
        return toResponse(saved);
    }

    private ContactMessageResponse toResponse(ContactMessage entity) {
        return ContactMessageResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .subject(entity.getSubject())
                .message(entity.getMessage())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private String normalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
