package com.meze.controller;

import com.meze.domains.ImageFile;
import com.meze.dto.ImageFileDTO;
import com.meze.dto.response.GPMResponse;
import com.meze.dto.response.ImageSavedResponse;
import com.meze.dto.response.ResponseMessage;
import com.meze.service.ImageFileService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;


@RestController
@RequestMapping("/image")
@AllArgsConstructor
public class ImageFileController {

    private ImageFileService imageFileService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ImageSavedResponse> uploadFile(@RequestParam("image") MultipartFile[] image){
       Set<String> images = imageFileService.saveImage(image);
       ImageSavedResponse response=new ImageSavedResponse
               (images, ResponseMessage.IMAGE_SAVED_RESPONSE_MESSAGE,true);
       return ResponseEntity.ok(response);
    }

    @PatchMapping("/showcase")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<GPMResponse> setShowcaseImage(@RequestParam("productId") Long productId,
                                                        @RequestParam("imageId") String imageId){
        imageFileService.setShowcaseImage(productId,imageId);
        GPMResponse response = new GPMResponse(ResponseMessage.IMAGE_SHOWCASE_RESPONSE_MESSAGE,true,null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> getImageFile(@PathVariable String id){
        ImageFile imageFile=imageFileService.getImageById(id);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename="+imageFile.getName()).body(imageFile.getImageData().getData());
    }

    @GetMapping("/display/{id}")
    public ResponseEntity<byte[]>displayImage(@PathVariable String id){
        ImageFile imageFile= imageFileService.getImageById(id);
        HttpHeaders header=new HttpHeaders();
        header.setContentType(MediaType.IMAGE_PNG);
        return new ResponseEntity<>(imageFile.getImageData().getData(), header, HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ImageFileDTO>> getAllImages(){
        List<ImageFileDTO> imageList=imageFileService.getAllImages();
        return ResponseEntity.ok(imageList);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GPMResponse>deleteImageFile(@PathVariable String id){
        imageFileService.removeById(id);
        GPMResponse response =new GPMResponse(ResponseMessage.IMAGE_DELETE_RESPONSE_MESSAGE,true);
                return ResponseEntity.ok(response);
    }
}
