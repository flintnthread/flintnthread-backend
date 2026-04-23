package com.ecommerce.authdemo.service;

import com.ecommerce.authdemo.dto.InvoiceRequest;
import com.ecommerce.authdemo.dto.InvoiceResponse;

import java.util.List;

public interface InvoiceService {
    InvoiceResponse create(InvoiceRequest request);

    List<InvoiceResponse> getByOrderId(Integer orderId);

    InvoiceResponse getByInvoiceNumber(String invoiceNumber);

    InvoiceResponse update(Integer id, InvoiceRequest request);

    void delete(Integer id);
}
