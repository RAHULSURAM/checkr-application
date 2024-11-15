package com.example.CheckrApplication.JPARepository;

import com.example.CheckrApplication.DAO.ReportDAO;
import com.example.CheckrApplication.DAO.ViolationDAO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ViolationRepository extends JpaRepository<ViolationDAO,Long> {
    List<ViolationDAO> findByReportDAO(ReportDAO reportDAO);
}
