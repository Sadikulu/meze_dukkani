package com.meze.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class GPMResponse {
    private String message;
    private Boolean success;
    private Object data;

    public GPMResponse(String message, boolean success) {
        this.message=message;
        this.success=success;

    }
}
