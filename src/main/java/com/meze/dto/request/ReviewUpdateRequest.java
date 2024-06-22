package com.meze.dto.request;

import com.meze.domains.enums.ReviewStatus;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewUpdateRequest {

    private ReviewStatus status;
}
