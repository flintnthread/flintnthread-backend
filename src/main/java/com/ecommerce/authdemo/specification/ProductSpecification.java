package com.ecommerce.authdemo.specification;

import com.ecommerce.authdemo.dto.ProductFilterRequestDTO;
import com.ecommerce.authdemo.entity.Product;
import com.ecommerce.authdemo.entity.ProductVariant;
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

            // Keyword search in name and description
            if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
                String keyword = "%" + request.getKeyword().toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("name")), keyword),
                    cb.like(cb.lower(root.get("shortDescription")), keyword),
                    cb.like(cb.lower(root.get("description")), keyword)
                ));
            }

            // Category filters
            if (request.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("categoryId"), request.getCategoryId()));
            }

            if (request.getSubcategoryId() != null) {
                predicates.add(cb.equal(root.get("subcategoryId"), request.getSubcategoryId()));
            }

            // Seller filter
            if (request.getSellerId() != null) {
                predicates.add(cb.equal(root.get("sellerId"), request.getSellerId()));
            }

            // Gender filter
            if (request.getGenders() != null && !request.getGenders().isEmpty()) {
                predicates.add(root.get("gender").in(request.getGenders()));
            }

            // Fabric filter
            if (request.getFabrics() != null && !request.getFabrics().isEmpty()) {
                predicates.add(root.get("productMaterialType").in(request.getFabrics()));
            }

            // Price range filter - Join with ProductVariant
            if (request.getMinPrice() != null || request.getMaxPrice() != null) {
                Join<Product, ProductVariant> variantJoin = root.join("variants", JoinType.INNER);
                
                if (request.getMinPrice() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(
                        variantJoin.get("sellingPrice"), request.getMinPrice()
                    ));
                }
                
                if (request.getMaxPrice() != null) {
                    predicates.add(cb.lessThanOrEqualTo(
                        variantJoin.get("sellingPrice"), request.getMaxPrice()
                    ));
                }
            }

            // Color filter - Join with ProductVariant
            if (request.getColors() != null && !request.getColors().isEmpty()) {
                Join<Product, ProductVariant> variantJoin = root.join("variants", JoinType.INNER);
                predicates.add(variantJoin.get("color").in(request.getColors()));
            }

            // Size filter - Join with ProductVariant
            if (request.getSizes() != null && !request.getSizes().isEmpty()) {
                Join<Product, ProductVariant> variantJoin = root.join("variants", JoinType.INNER);
                predicates.add(variantJoin.get("size").in(request.getSizes()));
            }

            // Stock filter
            if (request.getInStock() != null && request.getInStock()) {
                Join<Product, ProductVariant> variantJoin = root.join("variants", JoinType.INNER);
                predicates.add(cb.greaterThan(variantJoin.get("stock"), 0));
            }

            // Rating filter - This would require joining with reviews
            if (request.getMinRating() != null) {
                // For now, we'll skip rating filter as it requires complex join with reviews
                // This can be implemented later with a proper review join
            }

            // Remove duplicates when joining with variants
            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

