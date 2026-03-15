package com.ecommerce.authdemo.specification;

import com.ecommerce.authdemo.dto.ProductFilterRequestDTO;
import com.ecommerce.authdemo.entity.Product;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

    public class ProductSpecification {

        public static Specification<Product> filterProducts(ProductFilterRequestDTO request) {

            return (Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {

                List<Predicate> predicates = new ArrayList<>();

                // Only ACTIVE products
                predicates.add(cb.equal(root.get("status"), "active"));

                if (request.getCategoryId() != null) {
                    predicates.add(cb.equal(root.get("categoryId"), request.getCategoryId()));
                }

                if (request.getSubcategoryId() != null) {
                    predicates.add(cb.equal(root.get("subcategoryId"), request.getSubcategoryId()));
                }

                if (request.getSellerId() != null) {
                    predicates.add(cb.equal(root.get("sellerId"), request.getSellerId()));
                }

                if (request.getFabrics() != null) {
                    predicates.add(cb.equal(root.get("productMaterialType"), request.getFabrics()));
                }

                if (request.getGenders() != null) {
                    predicates.add(cb.equal(root.get("gender"), request.getGenders()));
                }

                return cb.and(predicates.toArray(new Predicate[0]));
            };
        }
    }

