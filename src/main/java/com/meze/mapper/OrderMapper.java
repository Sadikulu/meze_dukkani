package com.meze.mapper;

import com.meze.domains.Coupons;
import com.meze.domains.Order;
import com.meze.domains.OrderCoupon;
import com.meze.domains.User;
import com.meze.domains.enums.CouponsType;
import com.meze.dto.OrderDTO;
import com.meze.dto.request.OrderRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "orderItems",target = "orderItemsDTO")
    @Mapping(source = "user",target = "customer",qualifiedByName = "getUserFullName")
    @Mapping(source = "invoiceAddress",target = "invoiceAddressDTO")
    @Mapping(source = "shippingAddress",target = "shippingAddressDTO")
    @Mapping(target = "couponDiscount", expression = "java(getCouponDiscount(order.getOrderCoupons(),order.getSubTotal(),order.getDiscount(),order.getTax()))")
    OrderDTO orderToOrderDTO (Order order);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "status", ignore = true)
    Order orderRequestToOrder (OrderRequest orderRequest);

    List<OrderDTO> map (List<Order> orderList);

    @Named("getUserFullName")
    public static String getUserId(User user){
        return user.getFirstName() + " " +user.getLastName();
    }

    public default Double getCouponDiscount(Set<OrderCoupon> orderCoupon, double subTotal, double discount, double tax){
        double orderDiscount = 0.0;
        DecimalFormat df = new DecimalFormat("#.##");
        if (orderCoupon.size()>0){
            Coupons coupon = orderCoupon.stream().findFirst().get().getCoupons();
            if (coupon.getType().equals(CouponsType.EXACT_AMOUNT)){
                orderDiscount = coupon.getAmount();
            }else{
                orderDiscount = (((subTotal-discount)+tax)*coupon.getAmount())/100;
            }
        }
        return Double.parseDouble(df.format(orderDiscount).replaceAll(",","."));
    }

}