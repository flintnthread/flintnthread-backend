package com.ecommerce.authdemo.service.impl;

import com.ecommerce.authdemo.dto.LocationRequest;
import com.ecommerce.authdemo.entity.UserLocation;
import com.ecommerce.authdemo.repository.UserLocationRepository;
import com.ecommerce.authdemo.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final UserLocationRepository userLocationRepository;

    @Override
    public void saveLocation(LocationRequest request) {

        UserLocation location = new UserLocation();

        location.setUserId(request.getUserId());
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setCreatedAt(LocalDateTime.now());

        userLocationRepository.save(location);
    }
}