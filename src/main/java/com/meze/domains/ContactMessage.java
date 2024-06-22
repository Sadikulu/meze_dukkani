package com.meze.domains;

import javax.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Table(name="t_contact_message")
@Entity
public class ContactMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE) //setleme iptal edildi
    private Long id;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 50, nullable = false)
    private String subject;


    @Column(length = 200, nullable = false)
    private String body;


    @Column(length = 50, nullable = false)
    private String email;
}
