package com.meze.mapper;

import com.meze.domains.Coupons;
import com.meze.dto.CouponDTO;
import com.meze.dto.request.CouponRequest;
import com.meze.dto.request.CouponUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CouponsMapper {

//  Coupons ---> CouponDTO
    CouponDTO couponsToDto(Coupons coupons);

//  CouponRequest ---> Coupons
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    Coupons couponsRequestToCoupons(CouponRequest couponRequest);

//  CouponUpdateRequest ---> Coupons
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Coupons couponsUpdateRequestToCoupons(CouponUpdateRequest couponUpdateRequest);


//  List<Coupons>  --->   List<CouponDTO>
    List<CouponDTO> map(List<Coupons> couponsList);
}
