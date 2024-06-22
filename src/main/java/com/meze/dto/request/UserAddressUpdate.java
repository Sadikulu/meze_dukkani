package com.meze.dto.request;

import lombok.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserAddressUpdate {


    @Size(max=30,message="Size is exceeded")
    @NotBlank(message = "Please provide your title")
    private String title;

    @Size(min=2,max=30,message="Firstname is exceeded")
    @NotBlank(message = "Please provide your firstname")
    private String firstName;

    @Size(min=2,max=30,message="Lastname is exceeded")
    @NotBlank(message = "Please provide your lastname")
    private String lastName;

    @Pattern(regexp = "^((\\(\\d{3}\\))|\\d{3})[- .]?\\d{3}[- .]?\\d{4}$",
            message = "Please provide valid phone number")
    @Size(max=14)
    @NotBlank(message="Please provide your phone number")
    private String phone;

    @Size(min=10,max=80)
    @Email(message="Please provide your email")
    @NotBlank(message="Please provide your email")
    private String email;

    @Size(min=10,max=250)
    @NotBlank(message = "Please provide your address")
    private String address;

    @Size(min=2,max=70)
    @NotBlank(message = "Please provide your province")
    private String province;

    @Size(min=2,max=70)
    @NotBlank(message = "Please provide your city")
    private String city;

    @Size(min=2,max=70)
    @NotBlank(message = "Please provide your country")
    private String country;

    private LocalDateTime updateAt=LocalDateTime.now();

    //private Instant updateAt=Instant.now();

}
