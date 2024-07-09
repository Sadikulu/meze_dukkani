package com.meze.dto;

import com.meze.domains.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {

    private Long id;

//    private String sku;

    private String title;

//    private String shortDesc;

    private String longDesc;

    private Double price;

    private Double discountedPrice;

    private Double tax;

    private Integer discount;

    private Integer stockAmount;

//    private String slug;

    private Boolean featured;

    private Set<ShowcaseImageDTO> image;

    private Boolean newProduct;

    private ProductStatus status;

    private Boolean builtIn;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;

    private ProductCategoryDTO category;
}
