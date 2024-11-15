package com.example.CheckrApplication.JPARepository;

import com.example.CheckrApplication.DAO.CandidateDAO;
import com.example.CheckrApplication.DAO.ReportDAO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<ReportDAO,Long> {



    ArrayList<ReportDAO> findByCandidateDAO(CandidateDAO candidateDAO);
}
