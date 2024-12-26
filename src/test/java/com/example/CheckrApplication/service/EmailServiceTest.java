package com.example.CheckrApplication.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_SUBJECT = "Test Subject";
    private static final String TEST_BODY = "Test Body";
    private static final byte[] TEST_ATTACHMENT = "Test Attachment".getBytes();
    private static final String TEST_ATTACHMENT_NAME = "test.txt";

    @BeforeEach
    void setUp() throws MessagingException {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void sendEmailWithAttachment_Success() throws MessagingException {
        // Arrange
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // Act & Assert
        assertDoesNotThrow(() -> 
            emailService.sendEmailWithAttachment(
                TEST_EMAIL,
                TEST_SUBJECT,
                TEST_BODY,
                TEST_ATTACHMENT,
                TEST_ATTACHMENT_NAME
            )
        );

        // Verify
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendEmailWithoutAttachment_Success() throws MessagingException {
        // Arrange
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // Act & Assert
        assertDoesNotThrow(() -> 
            emailService.sendEmailWithAttachment(
                TEST_EMAIL,
                TEST_SUBJECT,
                TEST_BODY,
                null,
                null
            )
        );

        // Verify
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendEmail_WhenMailSenderThrowsException() throws MessagingException {
        // Arrange
        String errorMessage = "Failed to send email";
        doThrow(new MailSendException(errorMessage))
            .when(mailSender)
            .send(any(MimeMessage.class));

        // Act & Assert
        MailSendException exception = assertThrows(MailSendException.class, () ->
            emailService.sendEmailWithAttachment(
                TEST_EMAIL,
                TEST_SUBJECT,
                TEST_BODY,
                TEST_ATTACHMENT,
                TEST_ATTACHMENT_NAME
            )
        );

        assertTrue(exception.getMessage().contains(errorMessage));
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendEmail_WithInvalidEmail() throws MessagingException {
        // Arrange
        String errorMessage = "Invalid email address";
        doThrow(new MailSendException(errorMessage))
            .when(mailSender)
            .send(any(MimeMessage.class));

        // Act & Assert
        MailSendException exception = assertThrows(MailSendException.class, () ->
            emailService.sendEmailWithAttachment(
                "invalid-email",
                TEST_SUBJECT,
                TEST_BODY,
                TEST_ATTACHMENT,
                TEST_ATTACHMENT_NAME
            )
        );
        
        assertTrue(exception.getMessage().contains(errorMessage));
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }
}
