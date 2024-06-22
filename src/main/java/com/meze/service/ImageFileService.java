package com.meze.service;

import com.meze.domains.ImageData;
import com.meze.domains.ImageFile;
import com.meze.domains.Product;
import com.meze.dto.ImageFileDTO;
import com.meze.exception.ResourceNotFoundException;
import com.meze.exception.message.ErrorMessage;
import com.meze.exception.message.ImageFileException;
import com.meze.repository.ImageFileRepository;
import com.meze.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageFileService {

    private final ImageFileRepository imageFileRepository;
    private final ProductRepository productRepository;

    public Set<String> saveImage(MultipartFile[] file) {

        ImageFile imageFile=null;
        Set<ImageFile> images = new HashSet<>();

        for (MultipartFile each:file) {
            String fileName = null;
            try
            {
                imageFile = new ImageFile();
                fileName = StringUtils.cleanPath(Objects.requireNonNull(each.getOriginalFilename()));
                ImageData imageData = new ImageData(each.getBytes());
                imageFile.setName(fileName);
                imageFile.setType(each.getContentType());
                imageFile.setImageData(imageData);
                imageFile.setShowcase(false);

            }catch (IOException e){
                throw new ImageFileException(e.getMessage());
            }
            imageFileRepository.save(imageFile);
            images.add(imageFile);
        }

        return images.stream().map(ImageFile::getId).collect(Collectors.toSet());
    }

    public ImageFile getImageById(String id) {
        return imageFileRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(String.format(ErrorMessage.IMAGE_NOT_FOUND_MESSAGE, id)));
    }

    public List<ImageFileDTO> getAllImages() {
        List<ImageFile> imageList = imageFileRepository.findAll();
        return imageList.stream().map(imgFile ->{
           String imageUri= ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/image/download/")
                    .path(imgFile.getId())
                    .toUriString();
            return new ImageFileDTO(imgFile.getName(),imageUri,imgFile.getType());
        } ).collect(Collectors.toList());
    }


    public void removeById(String id) {
        ImageFile imageFile =  getImageById(id);
        imageFileRepository.delete(imageFile);
    }

    public void setShowcaseImage(Long productId,String imageId) {
        Product product = productRepository.findProductById(productId).orElseThrow(()->
                new ResourceNotFoundException(ErrorMessage.PRODUCT_NOT_FOUND_MESSAGE));
        ImageFile imageFile = getImageById(imageId);
        for (ImageFile each:product.getImage()) {
            each.setShowcase(false);
        }
        imageFile.setShowcase(true);
        imageFileRepository.save(imageFile);
    }
}
