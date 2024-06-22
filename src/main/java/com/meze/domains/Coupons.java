package com.meze.domains;

import com.meze.domains.enums.CouponsStatus;
import com.meze.domains.enums.CouponsType;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="t_coupons")
public class Coupons {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponsType type;

    @Column(nullable = false)
    private Integer life;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponsStatus status;

    @Column
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "coupons",orphanRemoval = true)
    private Set<OrderCoupon> orderCoupon = new HashSet<>();
}
