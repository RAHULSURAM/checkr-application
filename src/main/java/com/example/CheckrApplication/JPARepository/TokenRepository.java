package com.example.CheckrApplication.JPARepository;

import com.example.CheckrApplication.DAO.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    @Query("""
            SELECT t FROM Token t
            WHERE t.userDAO.id = :userId AND t.loggedOut = false
            """)
    List<Token> findAllTokenByUser(Long userId);

    Optional<Token> findByToken(String token);
}
