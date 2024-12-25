package com.example.CheckrApplication.controller;

import com.example.CheckrApplication.DTO.ForgotPasswordRequestDTO;
import com.example.CheckrApplication.DTO.ResetPasswordRequestDTO;
import com.example.CheckrApplication.exception.InvalidTokenException;
import com.example.CheckrApplication.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/checkr/auth/v1/fp")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        try {
            passwordResetService.initiatePasswordReset(request.getEmail());
            return ResponseEntity.ok(Map.of("message", 
                "If an account exists with this email, you will receive password reset instructions."));
        } catch (Exception e) {
            log.error("Error in forgot password request", e);
            return ResponseEntity.ok(Map.of("message", 
                "If an account exists with this email, you will receive password reset instructions."));
        }
    }

    @GetMapping("/verify-reset-token")
    public ResponseEntity<?> verifyToken(@RequestParam String token) {
        boolean isValid = passwordResetService.verifyToken(token);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        try {
            passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Password has been successfully reset"));
        } catch (InvalidTokenException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error in password reset", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to reset password. Please try again."));
        }
    }
}
