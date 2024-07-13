package com.meze.reusableMethods;

import org.springframework.stereotype.Service;

@Service
public class DiscountCalculator {

    public Double totalPriceWithDiscountCalculate(Integer quantity, Double price, Integer discount){
        return quantity*price*(100-discount)/100;
    }
}
