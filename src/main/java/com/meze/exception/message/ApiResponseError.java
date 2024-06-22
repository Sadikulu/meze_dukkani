package com.meze.exception.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@Setter
public class ApiResponseError {
    // AMACIM : customize error mesajlarını bu sınıf içinde tutacağız
    private HttpStatus status;

    @Setter(AccessLevel.PRIVATE)
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="dd-MM-yyyy HH:mm:ss")
    private LocalDateTime timestamp;

    // exception mesajı
    private String message;

    // request edilen end-pointi tutmak için
    private String requestURI;

    // private constructor
    private ApiResponseError() {
        timestamp = LocalDateTime.now();
    }

    public ApiResponseError(HttpStatus status) {
        this();
        this.message="Unexpected Error";
        this.status = status;
    }
    public ApiResponseError(HttpStatus status, String message, String requestURI) {
        this(status); // 1 parametreli olan constructorı çağırıyor
        this.message=message;
        this.requestURI = requestURI;
    }

}
