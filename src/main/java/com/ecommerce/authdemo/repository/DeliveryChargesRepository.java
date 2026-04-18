package com.ecommerce.authdemo.repository;

import com.ecommerce.authdemo.entity.DeliveryCharges;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;

import java.math.BigDecimal;
import java.util.Optional;

    public interface DeliveryChargesRepository extends JpaRepository<DeliveryCharges, Integer> {

        Optional<DeliveryCharges> findByWeightMinLessThanEqualAndWeightMaxGreaterThanEqual(
                BigDecimal weight, BigDecimal weight2);
    }



