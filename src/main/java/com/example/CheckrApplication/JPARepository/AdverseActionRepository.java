package com.example.CheckrApplication.JPARepository;

import com.example.CheckrApplication.DAO.AdverseActionDAO;
import com.example.CheckrApplication.DAO.CandidateDAO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdverseActionRepository extends JpaRepository<AdverseActionDAO, Long> {
    List<AdverseActionDAO> findByCandidateDAO(CandidateDAO candidateDAO);
}
