package com.meze.domains;

import com.meze.domains.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Table(name = "t_payments")
@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private double amount;

    @Column
    private String provider;

    @Enumerated(EnumType.STRING)
    @Column
    private PaymentStatus status;

    @Column(nullable = false)
    private LocalDateTime createAt=LocalDateTime.now();

    @Column
    private LocalDateTime updateAt;
}
