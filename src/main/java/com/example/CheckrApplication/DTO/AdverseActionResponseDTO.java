package com.example.CheckrApplication.DTO;

import com.example.CheckrApplication.enums.Status;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdverseActionResponseDTO {
    private String candidateName;
    private Status status;
    private LocalDateTime preNoticeSentAt;
    private LocalDateTime postNoticeSentAt;
}
