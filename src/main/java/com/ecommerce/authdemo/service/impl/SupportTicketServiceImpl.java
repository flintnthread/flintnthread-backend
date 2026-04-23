package com.ecommerce.authdemo.service.impl;

import com.ecommerce.authdemo.dto.SupportTicketRequest;
import com.ecommerce.authdemo.dto.SupportTicketResponse;
import com.ecommerce.authdemo.entity.SupportTicket;
import com.ecommerce.authdemo.exception.ResourceNotFoundException;
import com.ecommerce.authdemo.repository.SupportTicketRepository;
import com.ecommerce.authdemo.service.SupportTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SupportTicketServiceImpl implements SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;

    @Override
    public SupportTicketResponse create(SupportTicketRequest request) {
        SupportTicket entity = SupportTicket.builder()
                .customerId(request.getCustomerId())
                .subject(request.getSubject().trim())
                .type(request.getType().trim())
                .message(request.getMessage().trim())
                .orderId(request.getOrderId())
                .attachmentPath(normalize(request.getAttachmentPath()))
                .status("open")
                .build();
        return toResponse(supportTicketRepository.save(entity));
    }

    @Override
    public List<SupportTicketResponse> getTickets(Integer customerId, String status, String type) {
        return supportTicketRepository.findWithFilters(customerId, normalize(status), normalize(type))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public SupportTicketResponse updateStatus(Integer id, String status) {
        SupportTicket entity = supportTicketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found"));
        entity.setStatus(status.trim().toLowerCase(Locale.ROOT));
        return toResponse(supportTicketRepository.save(entity));
    }

    @Override
    public void delete(Integer id) {
        SupportTicket entity = supportTicketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found"));
        supportTicketRepository.delete(entity);
    }

    private SupportTicketResponse toResponse(SupportTicket entity) {
        return SupportTicketResponse.builder()
                .id(entity.getId())
                .customerId(entity.getCustomerId())
                .subject(entity.getSubject())
                .type(entity.getType())
                .message(entity.getMessage())
                .orderId(entity.getOrderId())
                .attachmentPath(entity.getAttachmentPath())
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
