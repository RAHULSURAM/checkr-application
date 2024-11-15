package com.example.CheckrApplication.DTO;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SigninRequestDTO {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    // Getters and Setters
}
