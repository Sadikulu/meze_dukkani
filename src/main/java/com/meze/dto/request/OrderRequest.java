package com.meze.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    @NotBlank
    private String contactName;
    @NotBlank
    private String phoneNumber;
    @NotNull
    private Long shippingAddressId;
    @NotNull
    private Long invoiceAddressId;
    private String couponCode;
}
