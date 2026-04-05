package com.ecommerce.authdemo.repository;

import com.ecommerce.authdemo.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Integer> {

    List<Address> findByUserId(Integer userId);

    Optional<Address> findByUserIdAndIsDefaultTrue(Integer userId);

    Optional<Address> findByIdAndUserId(Long id, Long userId);


}
