package com.example.CheckrApplication.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SigninResponseDTO {
    private String token;
    private UserDTO user;

    // Getters and Setters
}
