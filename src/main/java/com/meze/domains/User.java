package com.meze.domains;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.meze.domains.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@Table(name = "t_users")
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30 , nullable = false)
    private String firstName;

    @Column(length = 30 , nullable = false)
    private String lastName;

    @Column(length = 15 , nullable = false)
    private String phone;

    @Column
    private LocalDate birthDate;

    @Column(length = 80, nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column
    private UserStatus status;

    @Column(length = 120, nullable = false)
    private String password;

    @Column(nullable = false)
    private Boolean builtIn = false;

    @Column
    private LocalDateTime createAt = LocalDateTime.now();

    @Column
    private LocalDateTime updateAt;


    @ManyToMany
    @JoinTable(name = "t_user_roles",
                        joinColumns = @JoinColumn(name = "user_id"),
                        inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @OneToMany(orphanRemoval = true)
    @JoinColumn(name = "user_id")
    private List<Transaction> transactions = new ArrayList<>();

    @OneToMany(orphanRemoval = true, mappedBy = "user")
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "user",orphanRemoval = true)
    private Set<OrderCoupon> orderCoupons = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user",orphanRemoval = true)
    private Set<UserAddress> addresses = new HashSet<>();

    @OneToMany(mappedBy = "user",orphanRemoval = true)
    private Set<ConfirmationToken> confirmationTokens = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL)
    private ShoppingCart shoppingCart;

    @OneToMany(mappedBy = "user",orphanRemoval = true)
    private Set<PasswordResetToken> passwordResetTokens = new HashSet<>();

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "t_user_favorites",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id"))
    private List<Product> favoriteList = new ArrayList<>();
}
