package com.ecommerce.authdemo.mapper;

import com.ecommerce.authdemo.dto.ProductDTO;
import com.ecommerce.authdemo.dto.ProductImageDTO;
import com.ecommerce.authdemo.dto.ProductVariantDTO;
import com.ecommerce.authdemo.entity.Product;
import com.ecommerce.authdemo.entity.ProductVariant;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductMapper {

    public ProductDTO toDTO(Product p) {

        ProductDTO dto = new ProductDTO();

        // ------------------------
        // BASIC
        // ------------------------
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());

        if (p.getCategoryId() != null) {
            dto.setCategoryId(Long.valueOf(p.getCategoryId()));
        }

        if (p.getSubcategoryId() != null) {
            dto.setSubcategoryId(Long.valueOf(p.getSubcategoryId()));
        }

        dto.setSellerId(p.getSellerId());
        dto.setStatus(p.getStatus());
        dto.setCreatedAt(p.getCreatedAt());

        dto.setShortDescription(p.getShortDescription());
        dto.setFeatures(p.getFeatures());
        dto.setSpecifications(p.getSpecifications());
        dto.setReturnPolicy(p.getReturnPolicy());

        dto.setGstPercentage(p.getGstPercentage());
        dto.setProductWeight(p.getProductWeight());

        dto.setDeliveryTimeMin(p.getDeliveryTimeMin());
        dto.setDeliveryTimeMax(p.getDeliveryTimeMax());

        // ------------------------
        // VARIANTS (SAFE)
        // ------------------------
        if (p.getVariants() != null && !p.getVariants().isEmpty()) {

            List<ProductVariantDTO> variantDTOs = p.getVariants()
                    .stream()
                    .map(v -> {
                        ProductVariantDTO vd = new ProductVariantDTO();

                        vd.setId(v.getId());

                        // SAFE productId mapping
                        if (v.getProduct() != null) {
                            vd.setProductId(v.getProduct().getId());
                        }

                        vd.setColor(v.getColor());
                        vd.setSize(v.getSize());
                        vd.setSku(v.getSku());

                        vd.setMrpPrice(v.getMrpPrice());
                        vd.setSellingPrice(v.getSellingPrice());
                        vd.setFinalPrice(v.getFinalPrice());

                        vd.setDiscountPercentage(v.getDiscountPercentage());
                        vd.setDiscountAmount(v.getDiscountAmount());

                        vd.setTaxPercentage(v.getTaxPercentage());
                        vd.setTaxAmount(v.getTaxAmount());

                        vd.setStock(v.getStock());
                        vd.setInStock(v.getStock() != null && v.getStock() > 0);

                        vd.setVideoPath(v.getVideoPath());
                        vd.setWeight(v.getWeight());

                        return vd;
                    })
                    .collect(Collectors.toList());

            dto.setVariants(variantDTOs);
        }

        // ------------------------
        // IMAGES (SAFE)
        // ------------------------
        if (p.getImages() != null && !p.getImages().isEmpty()) {

            List<ProductImageDTO> imageDTOs = p.getImages()
                    .stream()
                    .map(img -> {
                        ProductImageDTO im = new ProductImageDTO();
                        im.setImagePath(img.getImagePath());
                        return im;
                    })
                    .collect(Collectors.toList());

            dto.setImages(imageDTOs);
        }

        return dto;
    }

    // ------------------------
    // DTO → ENTITY
    // ------------------------
    public Product toEntity(ProductDTO dto) {

        Product p = new Product();

        p.setName(dto.getName());
        p.setDescription(dto.getDescription());

        if (dto.getCategoryId() != null) {
            p.setCategoryId(Math.toIntExact(dto.getCategoryId()));
        }

        if (dto.getSubcategoryId() != null) {
            p.setSubcategoryId(Math.toIntExact(dto.getSubcategoryId()));
        }

        p.setSellerId(dto.getSellerId());

        return p;
    }
}