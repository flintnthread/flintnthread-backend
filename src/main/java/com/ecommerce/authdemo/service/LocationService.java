package com.ecommerce.authdemo.service;

import com.ecommerce.authdemo.dto.LocationRequest;
import org.springframework.stereotype.Service;


@Service
public interface LocationService {

    void saveLocation(LocationRequest request);

}