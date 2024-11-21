package com.example.CheckrApplication.repository;



import com.example.CheckrApplication.DAO.CandidateDAO;
import com.example.CheckrApplication.DAO.UserDAO;
import com.example.CheckrApplication.JPARepository.UserRepository;
import com.example.CheckrApplication.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@DataJpaTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest {

   @Autowired
    private UserRepository userRepository;

   @Mock
    private UserDAO userDAO;

   @BeforeEach
    void setup(){
       userDAO = new UserDAO();
       userDAO.setFirstName("John");
       userDAO.setLastName("Doe");
       userDAO.setEmail("john@test.com");
       userDAO.setPassword("dummypassword");
       userDAO.setRole(Role.USER);
       userDAO.setCreatedAt(LocalDateTime.now());
       userDAO.setUpdatedAt(LocalDateTime.now());
       userRepository.save(userDAO);
   }

    @Test
    void findByEmail_Success() {
        Optional<UserDAO> found = userRepository.findByEmail("john@test.com");
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("John");
        assertThat(found.get().getLastName()).isEqualTo("Doe");
    }

    @Test
    void findByEmail_NotFound() {
        Optional<UserDAO> found = userRepository.findByEmail("nonexistent@example.com");
        assertThat(found).isNotPresent();
    }

    @Test
    void existsByEmail_Success(){
       Boolean found = userRepository.existsByEmail("john@test.com");
       assertThat(found).isTrue();
    }

    @Test
    void existsByEmail_NotFound(){
       Boolean found = userRepository.existsByEmail("nonexistent@test.com");
       assertThat(found).isFalse();
    }
}