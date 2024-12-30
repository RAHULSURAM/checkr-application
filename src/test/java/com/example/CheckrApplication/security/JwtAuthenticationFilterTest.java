package com.example.CheckrApplication.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private JwtTokenProvider jwtService;

    @Mock
    private ApplicationContext context;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private SecurityContext securityContext;

    // @Mock
    // private UserPrincipal userPrincipal;

    private static final String VALID_TOKEN = "valid_token";
    private static final String TEST_USERNAME = "test@example.com";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
void doFilterInternal_WithValidToken_ShouldAuthenticate() throws ServletException, IOException {
    // Arrange
    String authHeader = "Bearer " + VALID_TOKEN;
    UserPrincipal userPrincipal = mock(UserPrincipal.class);
    // Fix: Specify the exact type for authorities
    Collection<SimpleGrantedAuthority> authorities = 
        Collections.singleton(new SimpleGrantedAuthority("USER"));
    
    when(request.getHeader("Authorization")).thenReturn(authHeader);
    when(jwtService.extractUserName(VALID_TOKEN)).thenReturn(TEST_USERNAME);
    when(securityContext.getAuthentication()).thenReturn(null);
    when(context.getBean(CustomUserDetailsService.class)).thenReturn(userDetailsService);
    when(userDetailsService.loadUserByUsername(TEST_USERNAME)).thenReturn(userPrincipal);
    when(jwtService.validateToken(VALID_TOKEN, userPrincipal)).thenReturn(true);
    // Fix: Match the exact return type from UserPrincipal
    when(userPrincipal.getAuthorities()).thenReturn(null);

    // Act
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Assert
    verify(securityContext).setAuthentication(any());
    verify(filterChain).doFilter(request, response);
}

    @Test
    void doFilterInternal_WithoutAuthHeader_ShouldContinueChain() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtService, never()).extractUserName(any());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithNonBearerToken_ShouldContinueChain() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Basic " + VALID_TOKEN);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtService, never()).extractUserName(any());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithInvalidToken_ShouldNotAuthenticate() throws ServletException, IOException {
        // Arrange
        String authHeader = "Bearer " + VALID_TOKEN;
        UserPrincipal userPrincipal = mock(UserPrincipal.class);
        
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUserName(VALID_TOKEN)).thenReturn(TEST_USERNAME);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(context.getBean(CustomUserDetailsService.class)).thenReturn(userDetailsService);
        when(userDetailsService.loadUserByUsername(TEST_USERNAME)).thenReturn(userPrincipal);
        when(jwtService.validateToken(VALID_TOKEN, userPrincipal)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithExistingAuthentication_ShouldContinueChain() throws ServletException, IOException {
        // Arrange
        String authHeader = "Bearer " + VALID_TOKEN;
        
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUserName(VALID_TOKEN)).thenReturn(TEST_USERNAME);
        when(securityContext.getAuthentication()).thenReturn(mock(org.springframework.security.core.Authentication.class));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(userDetailsService, never()).loadUserByUsername(any());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithEmptyUsername_ShouldContinueChain() throws ServletException, IOException {
        // Arrange
        String authHeader = "Bearer " + VALID_TOKEN;
        
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUserName(VALID_TOKEN)).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(userDetailsService, never()).loadUserByUsername(any());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }
}
