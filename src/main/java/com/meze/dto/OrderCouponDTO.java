package com.meze.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderCouponDTO {

    private String orderCode;

    private Double discountAmount;

    private String couponType;

    private Double orderGrandTotal;

}
