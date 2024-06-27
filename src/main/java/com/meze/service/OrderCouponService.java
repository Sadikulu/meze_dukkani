package com.meze.service;

import com.meze.domains.OrderCoupon;
import com.meze.dto.OrderCouponDTO;
import com.meze.mapper.OrderCouponMapper;
import com.meze.repository.OrderCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderCouponService {

    private final UserService userService;
    private final OrderCouponMapper orderCouponMapper;
    private final OrderCouponRepository orderCouponRepository;

    public Page<OrderCouponDTO> getAuthCouponsWithPage(Long id,Pageable pageable) {
        Page<OrderCoupon> orderCoupons = orderCouponRepository.findByUserId(id,pageable);
        return orderCoupons.map(orderCouponMapper::orderCouponToOrderCouponDTO);
    }
}
