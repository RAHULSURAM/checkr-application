package com.example.CheckrApplication.service;

import com.example.CheckrApplication.DAO.UserDAO;
import com.example.CheckrApplication.DTO.*;
import com.example.CheckrApplication.JPARepository.TokenRepository;
import com.example.CheckrApplication.JPARepository.UserRepository;
import com.example.CheckrApplication.exception.BadRequestException;
import com.example.CheckrApplication.exception.InvalidCredentialsException;
import com.example.CheckrApplication.exception.ResourceNotFoundException;
import com.example.CheckrApplication.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//@DataJpaTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class AuthenticationServiceTest {

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    private SignupRequestDTO signupRequestDTO;
    private UserDAO userDAO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

// Initialize SignupRequestDTO
        signupRequestDTO = new SignupRequestDTO();
        signupRequestDTO.setEmail("test@example.com");
        signupRequestDTO.setPassword("password123");
        signupRequestDTO.setConfirmPassword("password123");
        signupRequestDTO.setFirstName("John");
        signupRequestDTO.setLastName("Doe");

// Initialize UserDAO
        userDAO = new UserDAO();
        userDAO.setId(1L);
        userDAO.setEmail("test@example.com");
        userDAO.setPassword(new BCryptPasswordEncoder().encode("password123"));
        userDAO.setFirstName("John");
        userDAO.setLastName("Doe");
    }

    @Test
    void registerUser_Success() {
        // Arrange
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.save(any(UserDAO.class))).thenReturn(userDAO);

// Act
        assertDoesNotThrow(() -> authenticationService.registerUser(signupRequestDTO));

// Assert
        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(userRepository, times(1)).save(any(UserDAO.class));
    }

    @Test
    void registerUser_EmailAlreadyInUse() {
        // Arrange
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

// Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            authenticationService.registerUser(signupRequestDTO);
        });

        assertEquals("Email is already in use", exception.getMessage());
        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(userRepository, times(0)).save(any(UserDAO.class));
    }

    @Test
    void registerUser_PasswordsDoNotMatch() {
        // Arrange
        signupRequestDTO.setConfirmPassword("differentPassword");

// Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            authenticationService.registerUser(signupRequestDTO);
        });

        assertEquals("Passwords do not match", exception.getMessage());
//        verify(userRepository, times(0)).existsByEmail(anyString());
        verify(userRepository, times(0)).save(any(UserDAO.class));
    }

    @Test
    void authenticateUser_Success() {
        // Arrange
        SigninRequestDTO signinRequestDTO = new SigninRequestDTO();
        signinRequestDTO.setEmail("test@example.com");
        signinRequestDTO.setPassword("password123");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(tokenProvider.generateToken("test@example.com")).thenReturn("fake-jwt-token");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userDAO));
        when(tokenRepository.findAllTokenByUser(anyLong())).thenReturn(new ArrayList<>());
        when(tokenRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

// Act
        SigninResponseDTO response = authenticationService.authenticateUser(signinRequestDTO);

// Assert
        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());
        assertEquals("test@example.com", response.getUser().getEmail());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider, times(1)).generateToken("test@example.com");
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(tokenRepository, times(1)).findAllTokenByUser(anyLong());
    }

    @Test
    void authenticateUser_InvalidCredentials() {
        // Arrange
        SigninRequestDTO signinRequestDTO = new SigninRequestDTO();
        signinRequestDTO.setEmail("test@example.com");
        signinRequestDTO.setPassword("wrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new InvalidCredentialsException("Email or Password is wrong",signinRequestDTO.getEmail())); // Simulating failed authentication

// Act & Assert
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> {
            authenticationService.authenticateUser(signinRequestDTO);
        });

        assertEquals("Email or Password is wrong", exception.getMessage());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider, times(0)).generateToken(anyString());
        verify(userRepository, times(0)).findByEmail(anyString());
    }

    @Test
    void authenticateUser_UserNotFound() {
        // Arrange
        SigninRequestDTO signinRequestDTO = new SigninRequestDTO();
        signinRequestDTO.setEmail("nonexistent@example.com");
        signinRequestDTO.setPassword("password123");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(tokenProvider.generateToken("nonexistent@example.com")).thenReturn("fake-jwt-token");
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

// Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            authenticationService.authenticateUser(signinRequestDTO);
        });

        assertEquals("User not found with email : 'nonexistent@example.com'", exception.getMessage());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider, times(1)).generateToken("nonexistent@example.com");
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
    }
}