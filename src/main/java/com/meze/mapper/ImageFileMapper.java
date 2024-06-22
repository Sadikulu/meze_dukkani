package com.meze.mapper;

import com.meze.domains.ImageFile;
import com.meze.dto.ShowcaseImageDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ImageFileMapper {

    @Mapping(source = "id",target = "imageId")
    ShowcaseImageDTO imageFileToShowcaseImageDTO(ImageFile imageFile);
}
