package com.meze.dto.request;

import com.meze.domains.enums.CouponsStatus;
import com.meze.domains.enums.CouponsType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CouponUpdateRequest
{
    @NotNull(message = "Please provide amount")
    private Double amount;

    @NotNull(message = "Please provide type")
    private CouponsType type;

    @NotNull(message = "Please provide lifetime")
    private Integer life;

    @NotNull(message = "Please provide description")
    private CouponsStatus status;
}
