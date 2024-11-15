package com.example.CheckrApplication.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReportDTO {
    private Long id;
    private String status;
    private String packageName;
    private String adjudication;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private Integer turnaroundTime;

    // Getters and Setters
}