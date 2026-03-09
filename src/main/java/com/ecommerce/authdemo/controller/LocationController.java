package com.ecommerce.authdemo.controller;

import com.ecommerce.authdemo.dto.LocationRequest;
import com.ecommerce.authdemo.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping("/update")
    public String updateLocation(@RequestBody LocationRequest request) {

        locationService.saveLocation(request);

        return "Location saved successfully";

    }
}