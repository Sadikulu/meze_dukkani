package com.meze.mapper;

import com.meze.domains.Coupons;
import com.meze.domains.Order;
import com.meze.domains.OrderCoupon;
import com.meze.domains.enums.CouponsType;
import com.meze.dto.OrderCouponDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface OrderCouponMapper {


    @Mapping(source = "order",target = "orderCode",qualifiedByName = "getOrderCode")
    @Mapping(source = "coupons",target = "discountAmount",qualifiedByName = "getDiscountAmount")
    @Mapping(source = "coupons",target = "couponType",qualifiedByName = "getCouponType")
    @Mapping(source = "order",target = "orderGrandTotal",qualifiedByName = "getOrderGrandTotal")
    OrderCouponDTO orderCouponToOrderCouponDTO(OrderCoupon orderCoupon);

    @Named("getOrderCode")
    public static String getOrderCode(Order order){
        return order.getCode();
    }

    @Named("getDiscountAmount")
    public static Double getOrderCode(Coupons coupons){
        return coupons.getAmount();

    }

    @Named("getCouponType")
    public static String getCouponType(Coupons coupons){
        String couponType = "";
        if (coupons.getType().equals(CouponsType.EXACT_AMOUNT)){
            couponType = "Exact Amount";
        }else {
            couponType = "Percentage";
        }
        return couponType;
    }

    @Named("getOrderGrandTotal")
    public static Double getOrderGrandTotal(Order order){
        return order.getGrandTotal();

    }
}
