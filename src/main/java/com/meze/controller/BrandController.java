package com.meze.controller;

import com.meze.domains.enums.BrandStatus;
import com.meze.dto.BrandDTO;
import com.meze.dto.request.BrandRequest;
import com.meze.dto.request.BrandUpdateRequest;
import com.meze.dto.response.MezeResponse;
import com.meze.dto.response.ResponseMessage;
import com.meze.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/brands")
public class BrandController {

    private final BrandService brandService;

    @GetMapping("/option")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<BrandDTO>> getAllBrandsForOption(){
       return ResponseEntity.ok(brandService.getAllBrandList());
    }

    @GetMapping
    public ResponseEntity<PageImpl<BrandDTO>> getAllBrands(@RequestParam(value = "q",required = false)String query,
                                                           @RequestParam(value = "status",required = false)BrandStatus status,
                                                           @RequestParam("page") int page,
                                                           @RequestParam("size") int size,
                                                           @RequestParam("sort") String prop,
                                                           @RequestParam(value="direction",required = false,
                                                               defaultValue = "DESC")Sort.Direction direction){
        Pageable pageable= PageRequest.of(page,size,Sort.by(direction,prop));
        PageImpl<BrandDTO> brandDTOPage=brandService.getAllBrandsByPage(query,status,pageable);
        return ResponseEntity.ok(brandDTOPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrandDTO> getBrandById(@PathVariable("id") Long id){
        BrandDTO brandDTO=brandService.getBrandById(id);
        return ResponseEntity.ok(brandDTO);
    }

    @PostMapping("/{imageId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public  ResponseEntity<MezeResponse> createBrand(@PathVariable("imageId")String imageId, @Valid @RequestBody BrandRequest brandRequest){
        BrandDTO brandDTO = brandService.createBrand(imageId,brandRequest);
        MezeResponse gpmResponse=new MezeResponse(ResponseMessage.BRAND_CREATE_RESPONSE_MESSAGE,true,brandDTO);
        return ResponseEntity.ok(gpmResponse);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<MezeResponse> updateBrand(@Valid @PathVariable("id") Long id, @RequestBody BrandUpdateRequest brandUpdateRequest){
     BrandDTO brandDTO=brandService.updateBrand(id,brandUpdateRequest);
        MezeResponse gpmResponse=new MezeResponse(ResponseMessage.BRAND_UPDATE_RESPONSE_MESSAGE,true,brandDTO);
        return  ResponseEntity.ok(gpmResponse);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<MezeResponse> deleteBrandById(@PathVariable("id") Long id){
        BrandDTO brandDTO=brandService.deleteBrandById(id);
        MezeResponse gpmResponse=new MezeResponse(ResponseMessage.BRAND_DELETE_RESPONSE_MESSAGE,true,brandDTO);
        return ResponseEntity.ok(gpmResponse);
    }
}

