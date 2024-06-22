package com.meze.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemQuantityUpdateRequest {
    @NotNull
    private Long orderId;
    @NotNull
    private Long productId;
}
