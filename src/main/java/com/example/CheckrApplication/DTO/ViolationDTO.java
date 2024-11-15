package com.example.CheckrApplication.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ViolationDTO {
    private Long id;
    private String type;
    private String description;
    private String status;
    private LocalDate date;

    // Getters and Setters
}
