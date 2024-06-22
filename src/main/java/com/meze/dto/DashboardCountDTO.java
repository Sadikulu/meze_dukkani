package com.meze.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DashboardCountDTO {
    private long customerCount;
    private long categoryCount;
    private long productCount;
    private long orderCount;
    private long reviewCount;
    private long contactMessageCount;
}
