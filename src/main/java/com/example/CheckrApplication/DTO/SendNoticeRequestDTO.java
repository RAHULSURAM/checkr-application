package com.example.CheckrApplication.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class SendNoticeRequestDTO {
    private LocalDateTime preNoticeSentAt;
    private String subject;
    private String body;
    private List<String> charges;
    private Integer numberOfDays;

    // Getters and Setters
}
