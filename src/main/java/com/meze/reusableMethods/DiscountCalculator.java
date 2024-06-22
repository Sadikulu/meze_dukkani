package com.meze.reusableMethods;

import org.springframework.stereotype.Service;

@Service
public class DiscountCalculator {

    public Double totalPriceWithDiscountCalculate(Integer quantity, Double price){
        return quantity*price*(100)/100;
    }
}
