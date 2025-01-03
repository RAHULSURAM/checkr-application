package com.example.CheckrApplication.repository;

import com.example.CheckrApplication.DAO.CandidateDAO;
import com.example.CheckrApplication.enums.Adjudication;
import com.example.CheckrApplication.enums.Status;
import com.example.CheckrApplication.JPARepository.CandidateRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class CandidateRepositoryTest {

    @Autowired
    private CandidateRepository candidateRepository;

    @Mock
    private CandidateDAO candidateDAO;

    @BeforeEach
    void setUp() {
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
        candidateRepository.save(candidateDAO);
    }

    @Test
    void findByEmail_Success() {
        Optional<CandidateDAO> found = candidateRepository.findByEmail("jane@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Jane Doe");
    }

    @Test
    void findByEmail_NotFound() {
        Optional<CandidateDAO> found = candidateRepository.findByEmail("nonexistent@example.com");
        assertThat(found).isNotPresent();
    }

    @Test
    void existsBySocialSecurity_Success() {
        boolean exists = candidateRepository.existsBySocialSecurity("123-45-6789");
        assertThat(exists).isTrue();
    }

    @Test
    void existsBySocialSecurity_NotFound() {
        boolean exists = candidateRepository.existsBySocialSecurity("000-00-0000");
        assertThat(exists).isFalse();
    }

    @Test
    void findAll_Specification_Success() {
        Specification<CandidateDAO> spec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), Status.CLEAR);
        Iterable<CandidateDAO> results = candidateRepository.findAll(spec);
        assertThat(results).isNotEmpty();
    }

    @Test
    void findAll_Specification_NoResults() {
        candidateRepository.deleteAll();
        Specification<CandidateDAO> spec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), Status.CONSIDER);
        Iterable<CandidateDAO> results = candidateRepository.findAll(spec);
        assertThat(results).isEmpty();
    }

    @Test
    void findByCreatedAtBetween_ShouldReturnCandidatesInDateRange() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fromDate = now.minusDays(1);
        LocalDateTime toDate = now.plusDays(1);

        CandidateDAO candidate2 = new CandidateDAO();
        candidate2.setName("John Smith");
        candidate2.setEmail("john@example.com");
        candidate2.setDateOfBirth(LocalDate.of(1992, 2, 2));
        candidate2.setPhone("987-654-3210");
        candidate2.setZipcode("10002");
        candidate2.setSocialSecurity("987-65-4321");
        candidate2.setDriversLicense("D7654321");
        candidate2.setCreatedAt(now);
        candidate2.setLocation("Los Angeles");
        candidate2.setStatus(Status.CLEAR);
        candidate2.setAdjudication(Adjudication.ENGAGED);
        candidateRepository.save(candidate2);

        // Act
        List<CandidateDAO> candidates = candidateRepository.findByCreatedAtBetween(fromDate, toDate);

        // Assert
        assertThat(candidates).isNotNull();
        assertThat(candidates).hasSize(2);
        assertThat(candidates).extracting(CandidateDAO::getCreatedAt)
            .allSatisfy(createdAt -> {
                assertThat(createdAt).isAfterOrEqualTo(fromDate);
                assertThat(createdAt).isBeforeOrEqualTo(toDate);
            });
    }

    @Test
    void findByCreatedAtBetween_ShouldReturnEmptyListWhenNoMatchesFound() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fromDate = now.plusDays(1);
        LocalDateTime toDate = now.plusDays(2);

        // Act
        List<CandidateDAO> candidates = candidateRepository.findByCreatedAtBetween(fromDate, toDate);

        // Assert
        assertThat(candidates).isNotNull();
        assertThat(candidates).isEmpty();
    }

    @Test
    void findByCreatedAtBetween_ShouldHandleSameDayRange() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59);

        // Act
        List<CandidateDAO> candidates = candidateRepository.findByCreatedAtBetween(startOfDay, endOfDay);

        // Assert
        assertThat(candidates).isNotNull();
        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).getCreatedAt().toLocalDate())
            .isEqualTo(now.toLocalDate());
    }
}