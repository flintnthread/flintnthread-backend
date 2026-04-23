package com.ecommerce.authdemo.repository;

import com.ecommerce.authdemo.entity.DeliveryCharges;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface DeliveryChargesRepository extends JpaRepository<DeliveryCharges, Integer> {

    Optional<DeliveryCharges> findByWeightMinLessThanEqualAndWeightMaxGreaterThanEqual(
            BigDecimal weight, BigDecimal weight2);

    @Query("""
            SELECT d
            FROM DeliveryCharges d
            WHERE (:status IS NULL OR d.status = :status)
            ORDER BY d.weightMin ASC
            """)
    List<DeliveryCharges> findWithStatus(@Param("status") Boolean status);
}


