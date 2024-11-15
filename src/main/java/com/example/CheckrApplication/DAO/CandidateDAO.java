package com.example.CheckrApplication.DAO;

import com.example.CheckrApplication.enums.Adjudication;
import com.example.CheckrApplication.enums.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "candidates")
public class CandidateDAO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "phone")
    private String phone;

    @Column(name = "zipcode")
    private String zipcode;

    @Column(name = "social_security", unique = true)
    private String socialSecurity;

    @Column(name = "drivers_license", unique = true)
    private String driversLicense;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "city")
    private String location;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Adjudication adjudication;

    // Relationships

    @OneToMany(mappedBy = "candidateDAO", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ReportDAO> reports = new ArrayList<>();

    @OneToMany(mappedBy = "candidateDAO", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AdverseActionDAO> adverseActions = new ArrayList<>();
}

