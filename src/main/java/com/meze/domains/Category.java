package com.meze.domains;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.meze.domains.enums.CategoryStatus;
import javax.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;



@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="t_categories")
@Entity
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 80, nullable = false)
    private String title;
    @Enumerated(EnumType.STRING)
    @Column
    private CategoryStatus status;
    @Column(nullable = false)
    private  Boolean builtIn = false;
    @Column
    private LocalDateTime createAt=LocalDateTime.now();
    @Column
    private LocalDateTime updateAt;

    @JsonIgnore
    @OneToMany(mappedBy = "category")
    private List<Product> product = new ArrayList<>();



}

