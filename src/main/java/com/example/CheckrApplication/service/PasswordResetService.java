package com.example.CheckrApplication.service;

import com.example.CheckrApplication.DAO.PasswordResetTokenDAO;
import com.example.CheckrApplication.DAO.UserDAO;
import com.example.CheckrApplication.JPARepository.PasswordResetTokenRepository;
import com.example.CheckrApplication.JPARepository.UserRepository;
import com.example.CheckrApplication.exception.InvalidTokenException;
import com.example.CheckrApplication.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Slf4j
@Service
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Value("${app.password-reset.token.expiration-minutes}")
    private int tokenExpirationMinutes;

    private static final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public void initiatePasswordReset(String email) {
        // Find user by email (we'll return the same response regardless to prevent user enumeration)
        UserDAO user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Delete any existing tokens for this user
        tokenRepository.deleteByUserId(user.getId());

        // Generate new token
        String token = generateSecureToken();
        
        // Create and save token entity
        PasswordResetTokenDAO resetToken = new PasswordResetTokenDAO();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(tokenExpirationMinutes));
        tokenRepository.save(resetToken);

        // Send email with token
        String emailBody = String.format(
            "Hello,\n\nYour password reset token is: %s\n\n" +
            "This token will expire in %d minutes.\n\n" +
            "To reset your password, make a POST request to /api/auth/reset-password with:\n" +
            "{\n" +
            "    \"token\": \"%s\",\n" +
            "    \"newPassword\": \"your-new-password\"\n" +
            "}\n\n" +
            "If you didn't request this, please ignore this email.\n\n" +
            "Best regards,\nYour Application Team", 
            token, tokenExpirationMinutes, token);

        try {
            emailService.sendEmailWithAttachment(
                user.getEmail(),
                "Password Reset Token",
                emailBody,
                null,
                null
            );
        } catch (Exception e) {
            log.error("Failed to send password reset email to: " + email, e);
            throw new RuntimeException("Failed to send password reset email");
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetTokenDAO resetToken = tokenRepository.findByTokenAndUsedFalse(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired token"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Token has expired");
        }

        UserDAO user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        // Send confirmation email
        try {
            emailService.sendEmailWithAttachment(
                user.getEmail(),
                "Password Reset Successful",
                "Your password has been successfully reset. If you did not perform this action, please contact support immediately.",
                null,
                null
            );
        } catch (Exception e) {
            log.error("Failed to send password reset confirmation email to: " + user.getEmail(), e);
        }
    }

    public boolean verifyToken(String token) {
        return tokenRepository.findByTokenAndUsedFalse(token)
                .map(resetToken -> !resetToken.getExpiryDate().isBefore(LocalDateTime.now()))
                .orElse(false);
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
