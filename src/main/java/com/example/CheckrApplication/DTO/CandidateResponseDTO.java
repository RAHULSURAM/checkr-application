package com.example.CheckrApplication.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
public class CandidateResponseDTO {
    private Long id;
    private String name;
    private String email;
    private String status;
    private String adjudication;
    private String location;
    private Date dateInfo;

    // Getters and Setters
}
