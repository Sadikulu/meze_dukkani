package com.meze.controller;

import com.meze.dto.OrderCouponDTO;
import com.meze.service.OrderCouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order-coupon")
public class OrderCouponController {

    private final OrderCouponService orderCouponService;

    @GetMapping("/{userId}")
    public ResponseEntity<Page<OrderCouponDTO>> getUserOrderCoupons(@PathVariable("userId") Long userId,
                                                                    @RequestParam("page") int page,
                                                                    @RequestParam("size") int size,
                                                                    @RequestParam("sort") String prop,
                                                                    @RequestParam(value = "direction",
                                                                            required = false, defaultValue = "DESC") Sort.Direction direction){
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, prop));
        Page<OrderCouponDTO> orderCouponDTO = orderCouponService.getAuthCouponsWithPage(userId,pageable);
        return ResponseEntity.ok(orderCouponDTO);
    }
}
