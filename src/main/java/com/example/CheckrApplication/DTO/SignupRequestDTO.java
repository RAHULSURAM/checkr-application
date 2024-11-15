package com.example.CheckrApplication.DTO;

import jakarta.validation.constraints.*;
import jdk.jfr.Name;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequestDTO {

    @NotBlank(message = "firstName is mandatory")
    @Size(min = 3, max = 40, message = "firstName should be valid")
    private String firstName;

    @NotBlank(message = "lastName is mandatory")
    @Size(min = 3, max = 40, message = "lastName should be valid")
    private String lastName;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 6, max = 40, message = "Password should have at least 6 characters")
    private String password;

    @NotBlank(message = "Confirm password is mandatory")
    @Size(min = 6, max = 40, message = "Confirm password should have at least 6 characters")
    private String confirmPassword;

    // Getters and Setters
}
