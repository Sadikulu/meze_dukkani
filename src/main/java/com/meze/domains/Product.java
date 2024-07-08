package com.meze.domains;

import com.meze.domains.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "t_products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String sku;

    @Column(length = 150, nullable = false)
    private String title;

//    @Column(length = 500)
//    private String shortDesc;

    @Column(length = 3500)
    private String longDesc;

    @Column
    private String price;

    @Column
    private Double discountedPrice;

    @Column
    private Double tax;

    @Column
    private Integer discount;

    @Column
    private Integer stockAmount;

    @Column(length = 100, nullable = false)
    private String slug;

    @Column(nullable = false)
    private Boolean featured;

    @Column
    private Boolean newProduct;

    @Enumerated(EnumType.STRING)
    @Column
    private ProductStatus status;

    @Column
    private Boolean builtIn = false;

    @Column
    private LocalDateTime createAt = LocalDateTime.now();

    @Column
    private LocalDateTime updateAt;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToMany(mappedBy = "favoriteList")
    private List<User> userList = new ArrayList<>();

    @OneToMany(mappedBy = "product",cascade = CascadeType.ALL)
    private Set<ShoppingCartItem> shoppingCartItem = new HashSet<>();

    @OneToMany(mappedBy = "product",cascade = CascadeType.ALL)
    private Set<Review> reviews = new HashSet<>();

    @OneToMany(mappedBy = "product",cascade = CascadeType.ALL)
    private Set<OrderItem> orderItems = new HashSet<>();

    @OneToMany(orphanRemoval = true)
    @JoinColumn(name = "product_id")
    private Set<ImageFile> image;
}
