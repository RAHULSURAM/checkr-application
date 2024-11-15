package com.example.CheckrApplication.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class CandidateDTO {
    private Long id;
    private String name;
    private String email;
    private LocalDate dateOfBirth;
    private String phone;
    private String zipcode;
    private String socialSecurity;
    private String driversLicense;
    private String status;
    private String adjudication;
    private LocalDateTime createdAt;

    // Getters and Setters
}
