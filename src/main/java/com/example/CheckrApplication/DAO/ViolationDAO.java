package com.example.CheckrApplication.DAO;

import com.example.CheckrApplication.enums.Status;
import com.example.CheckrApplication.enums.ViolationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "violations")
public class ViolationDAO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "report_id", nullable = false)
    private ReportDAO reportDAO;

    @Enumerated(EnumType.STRING)
    private ViolationType type;

    private String description;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


}
