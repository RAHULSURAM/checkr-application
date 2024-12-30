package com.example.CheckrApplication.config;

import com.example.CheckrApplication.DAO.Token;
import com.example.CheckrApplication.JPARepository.TokenRepository;
import com.example.CheckrApplication.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomLogoutHandlerTest {

    @InjectMocks
    private CustomLogoutHandler customLogoutHandler;

    @Mock
    private JwtTokenProvider jwtService;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    private static final String VALID_TOKEN = "valid_token";
    private static final String TEST_USERNAME = "test@example.com";

    @BeforeEach
    void setUp() {
        // Common setup if needed
    }

    @Test
    void logout_WithValidToken_ShouldUpdateTokenStatus() {
        // Arrange
        String authHeader = "Bearer " + VALID_TOKEN;
        Token token = new Token();
        token.setToken(VALID_TOKEN);
        token.setLoggedOut(false);

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUserName(VALID_TOKEN)).thenReturn(TEST_USERNAME);
        when(tokenRepository.findByToken(VALID_TOKEN)).thenReturn(Optional.of(token));
        when(tokenRepository.save(any(Token.class))).thenReturn(token);

        // Act
        customLogoutHandler.logout(request, response, authentication);

        // Assert
        verify(tokenRepository).findByToken(VALID_TOKEN);
        verify(tokenRepository).save(argThat(savedToken -> 
            savedToken.getToken().equals(VALID_TOKEN) && savedToken.isLoggedOut()
        ));
    }

    @Test
    void logout_WithoutAuthorizationHeader_ShouldDoNothing() {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        customLogoutHandler.logout(request, response, authentication);

        // Assert
        verify(tokenRepository, never()).findByToken(any());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void logout_WithNonBearerToken_ShouldDoNothing() {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Basic " + VALID_TOKEN);

        // Act
        customLogoutHandler.logout(request, response, authentication);

        // Assert
        verify(tokenRepository, never()).findByToken(any());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void logout_WithNonExistentToken_ShouldNotUpdateRepository() {
        // Arrange
        String authHeader = "Bearer " + VALID_TOKEN;
        
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUserName(VALID_TOKEN)).thenReturn(TEST_USERNAME);
        when(tokenRepository.findByToken(VALID_TOKEN)).thenReturn(Optional.empty());

        // Act
        customLogoutHandler.logout(request, response, authentication);

        // Assert
        verify(tokenRepository).findByToken(VALID_TOKEN);
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void logout_WithInvalidTokenFormat_ShouldDoNothing() {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer ");  // Empty token after Bearer prefix

        // Act
        customLogoutHandler.logout(request, response, authentication);

        // Assert
        verify(jwtService, never()).extractUserName(any());
        verify(tokenRepository, never()).findByToken(any());
        verify(tokenRepository, never()).save(any());
    }
}
