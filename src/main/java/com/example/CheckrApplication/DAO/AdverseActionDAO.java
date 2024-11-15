package com.example.CheckrApplication.DAO;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "adverse_actions")
public class AdverseActionDAO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "candidate_id", nullable = false)
    private CandidateDAO candidateDAO;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserDAO userDAO;

    @Column(name = "pre_notice_sent_at")
    private LocalDateTime preNoticeSentAt;

    @Column(name = "post_notice_sent_at")
    private LocalDateTime postNoticeSentAt;

    @ElementCollection
    private List<String> charges;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(name = "number_of_days")
    private Integer numberOfDays;

    // Getters and Setters
}
