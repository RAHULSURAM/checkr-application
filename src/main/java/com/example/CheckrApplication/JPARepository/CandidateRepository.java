package com.example.CheckrApplication.JPARepository;

import com.example.CheckrApplication.DAO.CandidateDAO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<CandidateDAO,Long>, JpaSpecificationExecutor<CandidateDAO> {
    Optional<CandidateDAO> findByEmail(String email);

    Boolean existsBySocialSecurity(String socialSecurity);
}
