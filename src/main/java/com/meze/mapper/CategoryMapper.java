package com.meze.mapper;

import com.meze.domains.Category;
import com.meze.dto.CategoryDTO;
import com.meze.dto.ProductCategoryDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDTO categoryToCategoryDTO(Category category);

    ProductCategoryDTO categoryToProductCategoryDTO(Category category);

    List<CategoryDTO> categoryListToCategoryDTOList(List<Category> categories);


}
