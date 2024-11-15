package com.example.CheckrApplication.exception;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidCredentialsException extends RuntimeException {
    private String message;
    private String email;
    public InvalidCredentialsException(String message,  String email) {
        super(String.format("%s not found with %s ",
                email, message));
        this.email=email;
        this.message=message;
    }
}
