package com.meze.dto;

import com.meze.domains.enums.CategoryStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO {

    private Long id;

    private String title;

    private CategoryStatus status;

    private Boolean builtIn;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;
}
