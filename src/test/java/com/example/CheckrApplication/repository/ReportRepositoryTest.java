package com.example.CheckrApplication.repository;

import com.example.CheckrApplication.DAO.AdverseActionDAO;
import com.example.CheckrApplication.DAO.CandidateDAO;
import com.example.CheckrApplication.DAO.ReportDAO;
import com.example.CheckrApplication.DAO.ViolationDAO;
import com.example.CheckrApplication.JPARepository.AdverseActionRepository;
import com.example.CheckrApplication.JPARepository.CandidateRepository;
import com.example.CheckrApplication.JPARepository.ReportRepository;
import com.example.CheckrApplication.JPARepository.ViolationRepository;
import com.example.CheckrApplication.enums.Adjudication;
import com.example.CheckrApplication.enums.Status;
import com.example.CheckrApplication.enums.ViolationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ExtendWith(MockitoExtension.class)
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ReportRepositoryTest {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private ViolationRepository violationRepository;

    private ReportDAO reportDAO;
    private CandidateDAO candidateDAO;
    private CandidateDAO candidateDAO1;
    private ViolationDAO violationDAO;

    @BeforeEach
    void setup() {
        // First, create and save the Candidate
        candidateDAO = new CandidateDAO();
        candidateDAO.setName("Jane Doe");
        candidateDAO.setEmail("jane@example.com");
        candidateDAO.setDateOfBirth(LocalDate.of(1990, 1, 1));
        candidateDAO.setPhone("123-456-7890");
        candidateDAO.setZipcode("10001");
        candidateDAO.setSocialSecurity("123-45-6789");
        candidateDAO.setDriversLicense("D1234567");
        candidateDAO.setCreatedAt(LocalDateTime.now());
        candidateDAO.setLocation("New York");
        candidateDAO.setStatus(Status.CLEAR);
        candidateDAO.setAdjudication(Adjudication.ENGAGED);
        candidateDAO = candidateRepository.save(candidateDAO);

        // Then create and save the Report
        reportDAO = new ReportDAO();
        reportDAO.setPackageName("Some Package");
        reportDAO.setCandidateDAO(candidateDAO);
        reportDAO.setStatus(Status.CLEAR);
        reportDAO.setAdjudication(Adjudication.ENGAGED);
        reportDAO.setCreatedAt(LocalDateTime.now());
        reportDAO.setCompletedAt(LocalDateTime.now());
        reportDAO = reportRepository.save(reportDAO);

        // Finally create and save the Violation
        violationDAO = new ViolationDAO();
        violationDAO.setCreatedAt(LocalDateTime.now());
        violationDAO.setType(ViolationType.COUNTY_CRIMINAL);
        violationDAO.setStatus(Status.CONSIDER);
        violationDAO.setDescription("ufoeqhfqeoifh");
        violationDAO.setUpdatedAt(LocalDateTime.now());
        violationDAO.setReportDAO(reportDAO);
        violationDAO = violationRepository.save(violationDAO);

        // Update report with violations list
        List<ViolationDAO> violationsList = new ArrayList<>();
        violationsList.add(violationDAO);
        reportDAO.setViolations(violationsList);
        reportDAO = reportRepository.save(reportDAO);

        // Initialize candidateDAO1 for negative test case
        // First, create and save the Candidate
//        candidateDAO1 = new CandidateDAO();
//        candidateDAO1.setName("other Doe");
//        candidateDAO1.setEmail("other@example.com");
//        candidateDAO1.setDateOfBirth(LocalDate.of(1990, 1, 1));
//        candidateDAO1.setPhone("123-456-7890");
//        candidateDAO1.setZipcode("10001");
//        candidateDAO1.setSocialSecurity("123-45-6789");
//        candidateDAO1.setDriversLicense("D1234567");
//        candidateDAO1.setCreatedAt(LocalDateTime.now());
//        candidateDAO1.setLocation("New York");
//        candidateDAO1.setStatus(Status.CLEAR);
//        candidateDAO1.setAdjudication(Adjudication.ENGAGED);
//        candidateDAO1 = candidateRepository.save(candidateDAO1);

        // Then create and save the Report
//        reportDAO = new ReportDAO();
//        reportDAO.setPackageName("Some Package");
//        reportDAO.setCandidateDAO(candidateDAO);
//        reportDAO.setStatus(Status.CLEAR);
//        reportDAO.setAdjudication(Adjudication.ENGAGED);
//        reportDAO.setCreatedAt(LocalDateTime.now());
//        reportDAO.setCompletedAt(LocalDateTime.now());
//        reportDAO = reportRepository.save(reportDAO);
//
//        // Finally create and save the Violation
//        violationDAO = new ViolationDAO();
//        violationDAO.setCreatedAt(LocalDateTime.now());
//        violationDAO.setType(ViolationType.COUNTY_CRIMINAL);
//        violationDAO.setStatus(Status.CONSIDER);
//        violationDAO.setDescription("ufoeqhfqeoifh");
//        violationDAO.setUpdatedAt(LocalDateTime.now());
//        violationDAO.setReportDAO(reportDAO);
//        violationDAO = violationRepository.save(violationDAO);
//
//        // Update report with violations list
//        List<ViolationDAO> violationsList = new ArrayList<>();
//        violationsList.add(violationDAO);
//        reportDAO.setViolations(violationsList);
//        reportDAO = reportRepository.save(reportDAO);
//        // Set other required fields...
    }

    @Test
    void findByCandidateDAO_Success() {
        List<ReportDAO> found = reportRepository.findByCandidateDAO(candidateDAO);
        assertThat(found).isNotEmpty();
        assertThat(found.get(0).getCandidateDAO().getEmail()).isEqualTo("jane@example.com");
    }

//    @Test
//    void findByCandidateDAO_NotFound() {
//        List<ReportDAO> found = reportRepository.findByCandidateDAO(candidateDAO1);
//        assertThat(found.size()).isEqualTo(0);
//    }
}