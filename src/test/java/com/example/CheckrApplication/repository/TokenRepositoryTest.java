package com.example.CheckrApplication.repository;

import com.example.CheckrApplication.DAO.Token;
import com.example.CheckrApplication.DAO.UserDAO;
import com.example.CheckrApplication.JPARepository.TokenRepository;
import com.example.CheckrApplication.JPARepository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TokenRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    private UserDAO testUser;
    private Token testToken;

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
        testToken = new Token();
        testToken.setToken("test-token");
        testToken.setUserDAO(testUser);
        testToken.setLoggedOut(false);
        testToken = entityManager.persist(testToken);

        entityManager.flush();
    }

    @Test
    void findAllTokenByUser_WhenUserHasTokens_ShouldReturnTokens() {
        // Arrange
        // Create another active token for the same user
        Token secondToken = new Token();
        secondToken.setToken("second-token");
        secondToken.setUserDAO(testUser);
        secondToken.setLoggedOut(false);
        entityManager.persist(secondToken);

        // Create a logged out token
        Token loggedOutToken = new Token();
        loggedOutToken.setToken("logged-out-token");
        loggedOutToken.setUserDAO(testUser);
        loggedOutToken.setLoggedOut(true);
        entityManager.persist(loggedOutToken);

        entityManager.flush();

        // Act
        List<Token> activeTokens = tokenRepository.findAllTokenByUser(testUser.getId());

        // Assert
        assertThat(activeTokens).hasSize(2);
        assertThat(activeTokens).extracting(Token::getToken)
                .containsExactlyInAnyOrder("test-token", "second-token");
    }

    @Test
    void findAllTokenByUser_WhenUserHasNoActiveTokens_ShouldReturnEmptyList() {
        // Arrange
        testToken.setLoggedOut(true);
        entityManager.persist(testToken);
        entityManager.flush();

        // Act
        List<Token> activeTokens = tokenRepository.findAllTokenByUser(testUser.getId());

        // Assert
        assertThat(activeTokens).isEmpty();
    }

    @Test
    void findByToken_WhenTokenExists_ShouldReturnToken() {
        // Act
        Optional<Token> found = tokenRepository.findByToken("test-token");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getToken()).isEqualTo("test-token");
        assertThat(found.get().isLoggedOut()).isFalse();
    }

    @Test
    void findByToken_WhenTokenDoesNotExist_ShouldReturnEmpty() {
        // Act
        Optional<Token> found = tokenRepository.findByToken("non-existent-token");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    void findAllTokenByUser_WhenUserDoesNotExist_ShouldReturnEmptyList() {
        // Act
        List<Token> tokens = tokenRepository.findAllTokenByUser(999L);

        // Assert
        assertThat(tokens).isEmpty();
    }
}
