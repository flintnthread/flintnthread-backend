package com.ecommerce.authdemo.dto;

import lombok.Data;
import java.util.List;

@Data
public class CategoryWithSubDTO {

    private String categoryName;
    private List<SubCategoryResponseDTO> subcategories;

}