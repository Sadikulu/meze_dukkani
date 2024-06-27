package com.meze.controller;


import com.meze.dto.CouponDTO;
import com.meze.dto.request.CouponMailRequest;
import com.meze.dto.request.CouponRequest;
import com.meze.dto.request.CouponUpdateRequest;
import com.meze.dto.response.MezeResponse;
import com.meze.dto.response.ResponseMessage;
import com.meze.service.CouponsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponsController {

    private final CouponsService couponsService;


    @GetMapping("/{id}/admin")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<CouponDTO> getCouponWithPath(@PathVariable("id") Long id){
        CouponDTO couponDTO  = couponsService.findCoupon(id);
        return ResponseEntity.ok(couponDTO);
    }

    @GetMapping("auth/{couponCode}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CUSTOMER')")
    public ResponseEntity<CouponDTO> controlCouponWithCouponCode(@PathVariable("couponCode") String couponCode){
        CouponDTO couponDTO  = couponsService.findCouponByCouponCode(couponCode);
        return ResponseEntity.ok(couponDTO);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<CouponDTO>> getAllCoupons(){
        List<CouponDTO> coupons = couponsService.getAllCouponsDTO();
        return ResponseEntity.ok(coupons);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<CouponDTO>> getAllCouponsWithPage(@RequestParam("page") int page,
                                                                 @RequestParam("size") int size,
                                                                 @RequestParam("sort") String prop,
                                                                 @RequestParam(value = "direction", required = false,
                                                                         defaultValue = "DESC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, prop));
        Page<CouponDTO> pageDTO = couponsService.getAllCouponsWithPage(pageable);
        return ResponseEntity.ok(pageDTO);
    }

    @PostMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<MezeResponse> createCoupon(@Valid @RequestBody CouponRequest couponRequest){
        CouponDTO couponDTO = couponsService.saveCoupon(couponRequest);
        MezeResponse gpmResponse = new MezeResponse(ResponseMessage.COUPON_CREATE_RESPONSE, true, couponDTO);
        return ResponseEntity.ok(gpmResponse);
    }

    @PostMapping("/auth/send")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<MezeResponse> sendCoupon(@Valid @RequestBody CouponMailRequest couponMailRequest){
        couponsService.sendCoupon(couponMailRequest);
        MezeResponse gpmResponse = new MezeResponse(ResponseMessage.COUPON_MAIL_SENT_RESPONSE, true);
        return ResponseEntity.ok(gpmResponse);
    }

    @PutMapping("/{id}/admin")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<MezeResponse> updateCoupon(@PathVariable Long id,
                                                     @Valid @RequestBody CouponUpdateRequest couponUpdateRequest){
        CouponDTO couponDTO = couponsService.updateCoupon(id, couponUpdateRequest);
        MezeResponse gpmResponse = new MezeResponse(ResponseMessage.COUPON_UPDATE_RESPONSE, true, couponDTO);
        return ResponseEntity.ok(gpmResponse);
    }

    @DeleteMapping("/{id}/admin")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<MezeResponse> deleteCoupon(@PathVariable Long id){
        couponsService.deleteCoupon(id);
        MezeResponse gpmResponse = new MezeResponse(ResponseMessage.COUPON_DELETE_RESPONSE, true, null);

        return ResponseEntity.ok(gpmResponse);
    }
}