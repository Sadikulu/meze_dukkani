package com.meze.mapper;

import com.meze.domains.Brand;
import com.meze.domains.ImageFile;
import com.meze.dto.BrandDTO;
import com.meze.dto.ProductBrandDTO;
import com.meze.dto.request.BrandRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;


@Mapper(componentModel = "spring")
public interface BrandMapper {

    @Mapping(source = "image",target = "image",qualifiedByName = "getImageAsString")
    BrandDTO brandToBrandDTO(Brand brand);
    
    ProductBrandDTO brandtToProductBrandDTO(Brand brand);

    List<BrandDTO> brandListToBrandDTOList(List<Brand> brands);

    Brand brandRequestToBrand(BrandRequest brandRequest);


    @Named("getImageAsString")
    public static String getImage(ImageFile imageFile){
        return imageFile.getId();
    }


}
