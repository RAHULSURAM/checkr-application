package com.example.CheckrApplication.JPARepository;

import com.example.CheckrApplication.DAO.UserDAO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserDAO,Long> {
    Optional<UserDAO> findByEmail(String email);

    Boolean existsByEmail(String email);
}
