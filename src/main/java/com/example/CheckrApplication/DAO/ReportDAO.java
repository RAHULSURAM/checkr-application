package com.example.CheckrApplication.DAO;

import com.example.CheckrApplication.enums.Adjudication;
import com.example.CheckrApplication.enums.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "reports")
public class ReportDAO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private CandidateDAO candidateDAO;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "package")
    private String packageName;

    @Enumerated(EnumType.STRING)
    private Adjudication adjudication;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "turnaround_time")
    private Integer turnaroundTime;

    @OneToMany(mappedBy = "reportDAO", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ViolationDAO> violations = new ArrayList<>();


}
