package com.example.CheckrApplication.service;

import com.example.CheckrApplication.DAO.PasswordResetTokenDAO;
import com.example.CheckrApplication.DAO.UserDAO;
import com.example.CheckrApplication.JPARepository.PasswordResetTokenRepository;
import com.example.CheckrApplication.JPARepository.UserRepository;
import com.example.CheckrApplication.exception.InvalidTokenException;
import com.example.CheckrApplication.exception.ResourceNotFoundException;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private UserDAO testUser;
    private PasswordResetTokenDAO testToken;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(passwordResetService, "tokenExpirationMinutes", 60);

        testUser = new UserDAO();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("oldPassword");

        testToken = new PasswordResetTokenDAO();
        testToken.setId(1L);
        testToken.setToken("validToken");
        testToken.setUser(testUser);
        testToken.setUsed(false);
        testToken.setExpiryDate(LocalDateTime.now().plusMinutes(30));
    }

    @Test
    void initiatePasswordReset_Success() throws MessagingException {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(tokenRepository.save(any())).thenReturn(testToken);
        doNothing().when(emailService).sendEmailWithAttachment(anyString(), anyString(), anyString(), isNull(), isNull());

        assertDoesNotThrow(() -> passwordResetService.initiatePasswordReset("test@example.com"));

        verify(tokenRepository).deleteByUserId(testUser.getId());
        verify(tokenRepository).save(any());
        verify(emailService).sendEmailWithAttachment(eq("test@example.com"), anyString(), anyString(), isNull(), isNull());
    }

    @Test
    void initiatePasswordReset_UserNotFound() throws MessagingException {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
            () -> passwordResetService.initiatePasswordReset("nonexistent@example.com"));

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendEmailWithAttachment(anyString(), anyString(), anyString(), any(), any());
    }

    @Test
    void initiatePasswordReset_EmailFailure() throws MessagingException {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(tokenRepository.save(any())).thenReturn(testToken);
        doThrow(new MessagingException("Email sending failed"))
            .when(emailService).sendEmailWithAttachment(
                anyString(), 
                anyString(), 
                anyString(), 
                any(), 
                any()
            );

        assertThrows(RuntimeException.class, 
            () -> passwordResetService.initiatePasswordReset("test@example.com"));
    }

    @Test
    void resetPassword_Success() {
        when(tokenRepository.findByTokenAndUsedFalse(anyString())).thenReturn(Optional.of(testToken));
        when(passwordEncoder.encode(anyString())).thenReturn("newHashedPassword");

        assertDoesNotThrow(() -> 
            passwordResetService.resetPassword("validToken", "newPassword"));

        verify(userRepository).save(testUser);
        verify(tokenRepository).save(testToken);
        assertTrue(testToken.isUsed());
    }

    @Test
    void resetPassword_InvalidToken() {
        when(tokenRepository.findByTokenAndUsedFalse(anyString())).thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class, 
            () -> passwordResetService.resetPassword("invalidToken", "newPassword"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_ExpiredToken() {
        testToken.setExpiryDate(LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByTokenAndUsedFalse(anyString())).thenReturn(Optional.of(testToken));

        assertThrows(InvalidTokenException.class, 
            () -> passwordResetService.resetPassword("validToken", "newPassword"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void verifyToken_Valid() {
        when(tokenRepository.findByTokenAndUsedFalse(anyString())).thenReturn(Optional.of(testToken));

        assertTrue(passwordResetService.verifyToken("validToken"));
    }

    @Test
    void verifyToken_Invalid() {
        when(tokenRepository.findByTokenAndUsedFalse(anyString())).thenReturn(Optional.empty());

        assertFalse(passwordResetService.verifyToken("invalidToken"));
    }

    @Test
    void verifyToken_Expired() {
        testToken.setExpiryDate(LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByTokenAndUsedFalse(anyString())).thenReturn(Optional.of(testToken));

        assertFalse(passwordResetService.verifyToken("validToken"));
    }
}
