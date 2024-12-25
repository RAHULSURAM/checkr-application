package com.example.CheckrApplication.JPARepository;

import com.example.CheckrApplication.DAO.PasswordResetTokenDAO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenDAO, Long> {
    Optional<PasswordResetTokenDAO> findByTokenAndUsedFalse(String token);
    void deleteByUserId(Long userId);
}
