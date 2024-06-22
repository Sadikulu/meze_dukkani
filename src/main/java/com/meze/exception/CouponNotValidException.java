package com.meze.exception;

public class CouponNotValidException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CouponNotValidException(String message){super(message);}
}
