package com.ecommerce.authdemo.service;

import com.ecommerce.authdemo.dto.SupportTicketRequest;
import com.ecommerce.authdemo.dto.SupportTicketResponse;

import java.util.List;

public interface SupportTicketService {
    SupportTicketResponse create(SupportTicketRequest request);

    List<SupportTicketResponse> getTickets(Integer customerId, String status, String type);

    SupportTicketResponse updateStatus(Integer id, String status);

    void delete(Integer id);
}
