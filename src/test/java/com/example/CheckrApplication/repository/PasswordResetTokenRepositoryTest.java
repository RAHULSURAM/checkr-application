package com.example.CheckrApplication.repository;

import com.example.CheckrApplication.DAO.PasswordResetTokenDAO;
import com.example.CheckrApplication.DAO.UserDAO;
import com.example.CheckrApplication.JPARepository.PasswordResetTokenRepository;
import com.example.CheckrApplication.JPARepository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PasswordResetTokenRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private UserRepository userRepository;

    private UserDAO testUser;
    private PasswordResetTokenDAO testToken;

    @BeforeEach
    void setUp() {
        // Create and persist a test user
        testUser = new UserDAO();
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword("password");
        testUser = entityManager.persist(testUser);

        // Create and persist a test token
        testToken = new PasswordResetTokenDAO();
        testToken.setToken("test-token");
        testToken.setUser(testUser);
        testToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        testToken.setUsed(false);
        testToken = entityManager.persist(testToken);

        entityManager.flush();
    }

    @Test
    void findByTokenAndUsedFalse_WhenTokenExists_ShouldReturnToken() {
        // Act
        Optional<PasswordResetTokenDAO> found = passwordResetTokenRepository.findByTokenAndUsedFalse("test-token");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getToken()).isEqualTo("test-token");
        assertThat(found.get().isUsed()).isFalse();
    }

    @Test
    void findByTokenAndUsedFalse_WhenTokenDoesNotExist_ShouldReturnEmpty() {
        // Act
        Optional<PasswordResetTokenDAO> found = passwordResetTokenRepository.findByTokenAndUsedFalse("non-existent-token");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    void findByTokenAndUsedFalse_WhenTokenIsUsed_ShouldReturnEmpty() {
        // Arrange
        testToken.setUsed(true);
        entityManager.persist(testToken);
        entityManager.flush();

        // Act
        Optional<PasswordResetTokenDAO> found = passwordResetTokenRepository.findByTokenAndUsedFalse("test-token");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    void deleteByUserId_ShouldDeleteAllUserTokens() {
        // Arrange
        // Create another token for the same user
        PasswordResetTokenDAO secondToken = new PasswordResetTokenDAO();
        secondToken.setToken("second-token");
        secondToken.setUser(testUser);
        secondToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        secondToken.setUsed(false);
        entityManager.persist(secondToken);
        entityManager.flush();

        // Act
        passwordResetTokenRepository.deleteByUserId(testUser.getId());
        entityManager.flush();

        // Assert
        List<PasswordResetTokenDAO> remainingTokens = entityManager
            .getEntityManager()
            .createQuery("SELECT t FROM PasswordResetTokenDAO t WHERE t.user.id = :userId", PasswordResetTokenDAO.class)
            .setParameter("userId", testUser.getId())
            .getResultList();

        assertThat(remainingTokens).isEmpty();
    }
}
