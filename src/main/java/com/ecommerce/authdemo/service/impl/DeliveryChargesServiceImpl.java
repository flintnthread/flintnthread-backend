package com.ecommerce.authdemo.service.impl;

import com.ecommerce.authdemo.dto.DeliveryChargeRequest;
import com.ecommerce.authdemo.dto.DeliveryChargeResponse;
import com.ecommerce.authdemo.entity.DeliveryCharges;
import com.ecommerce.authdemo.exception.OrderException;
import com.ecommerce.authdemo.exception.ResourceNotFoundException;
import com.ecommerce.authdemo.repository.DeliveryChargesRepository;
import com.ecommerce.authdemo.service.DeliveryChargesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryChargesServiceImpl implements DeliveryChargesService {

    private final DeliveryChargesRepository deliveryChargesRepository;

    @Override
    public DeliveryChargeResponse create(DeliveryChargeRequest request) {
        validate(request);
        DeliveryCharges entity = DeliveryCharges.builder()
                .weightSlab(request.getWeightSlab().trim())
                .weightMin(request.getWeightMin())
                .weightMax(request.getWeightMax())
                .intraCityCharge(request.getIntraCityCharge())
                .metroMetroCharge(request.getMetroMetroCharge())
                .isCustom(request.getIsCustom() != null ? request.getIsCustom() : Boolean.FALSE)
                .status(request.getStatus() != null ? request.getStatus() : Boolean.TRUE)
                .build();

        return toResponse(deliveryChargesRepository.save(entity));
    }

    @Override
    public List<DeliveryChargeResponse> getAll(Boolean status) {
        return deliveryChargesRepository.findWithStatus(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public DeliveryChargeResponse update(Integer id, DeliveryChargeRequest request) {
        validate(request);
        DeliveryCharges entity = deliveryChargesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery charge slab not found"));

        entity.setWeightSlab(request.getWeightSlab().trim());
        entity.setWeightMin(request.getWeightMin());
        entity.setWeightMax(request.getWeightMax());
        entity.setIntraCityCharge(request.getIntraCityCharge());
        entity.setMetroMetroCharge(request.getMetroMetroCharge());
        entity.setIsCustom(request.getIsCustom() != null ? request.getIsCustom() : entity.getIsCustom());
        entity.setStatus(request.getStatus() != null ? request.getStatus() : entity.getStatus());

        return toResponse(deliveryChargesRepository.save(entity));
    }

    @Override
    public DeliveryChargeResponse updateStatus(Integer id, Boolean status) {
        DeliveryCharges entity = deliveryChargesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery charge slab not found"));
        entity.setStatus(status);
        return toResponse(deliveryChargesRepository.save(entity));
    }

    @Override
    public DeliveryChargeResponse getByWeight(BigDecimal weight) {
        if (weight == null || weight.compareTo(BigDecimal.ZERO) < 0) {
            throw new OrderException("Valid weight is required");
        }
        DeliveryCharges entity = deliveryChargesRepository
                .findByWeightMinLessThanEqualAndWeightMaxGreaterThanEqual(weight, weight)
                .filter(DeliveryCharges::getStatus)
                .orElseThrow(() -> new ResourceNotFoundException("No delivery slab found for weight " + weight));
        return toResponse(entity);
    }

    private void validate(DeliveryChargeRequest request) {
        if (request.getWeightMin().compareTo(request.getWeightMax()) > 0) {
            throw new OrderException("weightMin cannot be greater than weightMax");
        }
    }

    private DeliveryChargeResponse toResponse(DeliveryCharges entity) {
        return DeliveryChargeResponse.builder()
                .id(entity.getId())
                .weightSlab(entity.getWeightSlab())
                .weightMin(entity.getWeightMin())
                .weightMax(entity.getWeightMax())
                .intraCityCharge(entity.getIntraCityCharge())
                .metroMetroCharge(entity.getMetroMetroCharge())
                .isCustom(entity.getIsCustom())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
