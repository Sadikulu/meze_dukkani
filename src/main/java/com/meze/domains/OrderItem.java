package com.meze.domains;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@Table(name="t_order_items")
@Entity
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100,nullable = false)
    private String sku;

    @Column(nullable=false)
    private Integer quantity;

    @Column(nullable=false)
    private Double unitPrice;

    @Column
    private Double tax;

    @Column
    private Integer discount;

    @Column
    private Double subTotal;


    @Column
    private LocalDateTime createAt=LocalDateTime.now();


    @Column
    private LocalDateTime updateAt;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
