package com.example.CheckrApplication.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class AdverseActionDTO {
    private Long candidateId;
    private Long userId;
    private LocalDateTime preNoticeSentAt;
    private LocalDateTime postNoticeSentAt;
    private List<String> charges;
    private String subject;
    private String body;
    private Integer numberOfDays;

    // Getters and Setters
}
