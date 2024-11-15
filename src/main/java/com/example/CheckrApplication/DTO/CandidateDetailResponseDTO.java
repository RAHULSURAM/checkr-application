package com.example.CheckrApplication.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CandidateDetailResponseDTO {
    private CandidateDTO candidate;
    private ReportDTO report;
    private List<ViolationDTO> violations;

    // Getters and Setters
}