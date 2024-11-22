package com.example.CheckrApplication.security;

import com.example.CheckrApplication.DAO.UserDAO;
import com.example.CheckrApplication.JPARepository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class CustomUserDetailsServiceTest {

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private UserRepository userRepository;

    private UserDAO userDAO;

    @BeforeEach
    void setUp() {
        // Initialize Mockito annotations
        MockitoAnnotations.openMocks(this);

// Setup a sample UserDAO
        userDAO = new UserDAO();
        userDAO.setId(1L);
        userDAO.setFirstName("John");
        userDAO.setLastName("Doe");
        userDAO.setEmail("john.doe@example.com");
        userDAO.setPassword("securePassword123");
        // Initialize other necessary fields if any
    }

    @Test
    @DisplayName("Should load user by username/email when user exists")
    void testLoadUserByUsername_UserExists() {
        // Arrange
        String usernameOrEmail = "john.doe@example.com";
        when(userRepository.findByEmail(usernameOrEmail)).thenReturn(Optional.of(userDAO));

// Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(usernameOrEmail);

// Assert
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("john.doe@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("securePassword123");
        assertThat(userDetails.getAuthorities()).extracting("authority").containsExactly("USER");

// Verify repository interaction
        verify(userRepository, times(1)).findByEmail(usernameOrEmail);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user does not exist by username/email")
    void testLoadUserByUsername_UserDoesNotExist() {
        // Arrange
        String usernameOrEmail = "nonexistent@example.com";
        when(userRepository.findByEmail(usernameOrEmail)).thenReturn(Optional.empty());

// Act & Assert
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(usernameOrEmail))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with username or email: " + usernameOrEmail);

// Verify repository interaction
        verify(userRepository, times(1)).findByEmail(usernameOrEmail);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Should load user by ID when user exists")
    void testLoadUserById_UserExists() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(userDAO));

// Act
        UserDetails userDetails = customUserDetailsService.loadUserById(userId);

// Assert
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("john.doe@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("securePassword123");
        assertThat(userDetails.getAuthorities()).extracting("authority").containsExactly("USER");

// Verify repository interaction
        verify(userRepository, times(1)).findById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user does not exist by ID")
    void testLoadUserById_UserDoesNotExist() {
        // Arrange
        Long userId = 2L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

// Act & Assert
        assertThatThrownBy(() -> customUserDetailsService.loadUserById(userId))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with id: " + userId);

// Verify repository interaction
        verify(userRepository, times(1)).findById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Should handle null username/email gracefully by throwing exception")
    void testLoadUserByUsername_NullUsername() {
        // Arrange
        String usernameOrEmail = null;

// Act & Assert
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(usernameOrEmail))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with username or email: null");

// Verify repository interaction
        verify(userRepository, times(1)).findByEmail(usernameOrEmail);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Should handle null user ID gracefully by throwing exception")
    void testLoadUserById_NullId() {
        // Arrange
        Long userId = null;

// Act & Assert
        assertThatThrownBy(() -> customUserDetailsService.loadUserById(userId))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with id: null");


// Verify repository interaction
        verify(userRepository, times(1)).findById(userId);
        verifyNoMoreInteractions(userRepository);
    }
}
