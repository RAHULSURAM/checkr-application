package com.example.CheckrApplication.controller;

import com.example.CheckrApplication.DTO.ForgotPasswordRequestDTO;
import com.example.CheckrApplication.DTO.ResetPasswordRequestDTO;
import com.example.CheckrApplication.exception.InvalidTokenException;
import com.example.CheckrApplication.service.PasswordResetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetControllerTest {

    @Mock
    private PasswordResetService passwordResetService;

    @InjectMocks
    private PasswordResetController passwordResetController;

    private ForgotPasswordRequestDTO forgotPasswordRequest;
    private ResetPasswordRequestDTO resetPasswordRequest;
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_TOKEN = "valid-token";
    private static final String TEST_PASSWORD = "newPassword123@";

    @BeforeEach
    void setUp() {
        // Initialize forgotPasswordRequest
        forgotPasswordRequest = new ForgotPasswordRequestDTO();
        forgotPasswordRequest.setEmail(TEST_EMAIL);

        // Initialize resetPasswordRequest
        resetPasswordRequest = new ResetPasswordRequestDTO();
        resetPasswordRequest.setToken(TEST_TOKEN);
        resetPasswordRequest.setNewPassword(TEST_PASSWORD);
    }

    @Test
    void forgotPassword_Success() {
        // Arrange
        doNothing().when(passwordResetService).initiatePasswordReset(TEST_EMAIL);

        // Act
        ResponseEntity<?> response = passwordResetController.forgotPassword(forgotPasswordRequest);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertNotNull(responseBody);
        assertEquals("If an account exists with this email, you will receive password reset instructions.", 
            responseBody.get("message"));
        verify(passwordResetService, times(1)).initiatePasswordReset(TEST_EMAIL);
    }

    @Test
    void forgotPassword_WhenExceptionOccurs_ShouldReturnSameMessageForSecurity() {
        // Arrange
        doThrow(new RuntimeException("Error occurred"))
            .when(passwordResetService)
            .initiatePasswordReset(TEST_EMAIL);

        // Act
        ResponseEntity<?> response = passwordResetController.forgotPassword(forgotPasswordRequest);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertNotNull(responseBody);
        assertEquals("If an account exists with this email, you will receive password reset instructions.", 
            responseBody.get("message"));
        verify(passwordResetService, times(1)).initiatePasswordReset(TEST_EMAIL);
    }

    @Test
    void verifyToken_WhenTokenValid() {
        // Arrange
        when(passwordResetService.verifyToken(TEST_TOKEN)).thenReturn(true);

        // Act
        ResponseEntity<?> response = passwordResetController.verifyToken(TEST_TOKEN);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        @SuppressWarnings("unchecked")
        Map<String, Boolean> responseBody = (Map<String, Boolean>) response.getBody();
        assertNotNull(responseBody);
        assertTrue(responseBody.get("valid"));
        verify(passwordResetService, times(1)).verifyToken(TEST_TOKEN);
    }

    @Test
    void verifyToken_WhenTokenInvalid() {
        // Arrange
        when(passwordResetService.verifyToken(TEST_TOKEN)).thenReturn(false);

        // Act
        ResponseEntity<?> response = passwordResetController.verifyToken(TEST_TOKEN);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        @SuppressWarnings("unchecked")
        Map<String, Boolean> responseBody = (Map<String, Boolean>) response.getBody();
        assertNotNull(responseBody);
        assertFalse(responseBody.get("valid"));
        verify(passwordResetService, times(1)).verifyToken(TEST_TOKEN);
    }

    @Test
    void resetPassword_Success() {
        // Arrange
        doNothing().when(passwordResetService)
            .resetPassword(TEST_TOKEN, TEST_PASSWORD);

        // Act
        ResponseEntity<?> response = passwordResetController.resetPassword(resetPasswordRequest);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertNotNull(responseBody);
        assertEquals("Password has been successfully reset", responseBody.get("message"));
        verify(passwordResetService, times(1)).resetPassword(TEST_TOKEN, TEST_PASSWORD);
    }

    @Test
    void resetPassword_WhenTokenInvalid() {
        // Arrange
        String errorMessage = "Invalid or expired token";
        doThrow(new InvalidTokenException(errorMessage))
            .when(passwordResetService)
            .resetPassword(TEST_TOKEN, TEST_PASSWORD);

        // Act
        ResponseEntity<?> response = passwordResetController.resetPassword(resetPasswordRequest);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertNotNull(responseBody);
        assertEquals(errorMessage, responseBody.get("error"));
        verify(passwordResetService, times(1)).resetPassword(TEST_TOKEN, TEST_PASSWORD);
    }

    @Test
    void resetPassword_WhenUnexpectedError() {
        // Arrange
        doThrow(new RuntimeException("Unexpected error"))
            .when(passwordResetService)
            .resetPassword(TEST_TOKEN, TEST_PASSWORD);

        // Act
        ResponseEntity<?> response = passwordResetController.resetPassword(resetPasswordRequest);

        // Assert
        assertEquals(500, response.getStatusCodeValue());
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertNotNull(responseBody);
        assertEquals("Failed to reset password. Please try again.", responseBody.get("error"));
        verify(passwordResetService, times(1)).resetPassword(TEST_TOKEN, TEST_PASSWORD);
    }
}
