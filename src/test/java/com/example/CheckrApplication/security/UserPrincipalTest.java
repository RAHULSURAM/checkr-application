package com.example.CheckrApplication.security;

import com.example.CheckrApplication.DAO.UserDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class UserPrincipalTest {

    private UserDAO mockUserDAO;
    @Autowired
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        // Initialize the mock UserDAO
        mockUserDAO = mock(UserDAO.class);

        // Define behavior for mockUserDAO
        when(mockUserDAO.getId()).thenReturn(1L);
        when(mockUserDAO.getFirstName()).thenReturn("John");
        when(mockUserDAO.getLastName()).thenReturn("Doe");
        when(mockUserDAO.getEmail()).thenReturn("john.doe@example.com");
        when(mockUserDAO.getPassword()).thenReturn("securePassword123");

// Initialize UserPrincipal with the mocked UserDAO
        userPrincipal = new UserPrincipal(mockUserDAO);
    }

    @Test
    @DisplayName("Constructor should correctly set all fields from UserDAO")
    void testConstructor() {
        assertThat(userPrincipal.getId()).isEqualTo(1L);
        assertThat(userPrincipal.getName()).isEqualTo("John Doe");
        assertThat(userPrincipal.getUsername()).isEqualTo("john.doe@example.com");
        assertThat(userPrincipal.getPassword()).isEqualTo("securePassword123");
    }

    @Test
    @DisplayName("getAuthorities should return a collection with a single USER authority")
    void testGetAuthorities() {
        Collection<? extends GrantedAuthority> authorities = userPrincipal.getAuthorities();

        assertThat(authorities).hasSize(1);
        GrantedAuthority authority = authorities.iterator().next();
        assertThat(authority).isInstanceOf(SimpleGrantedAuthority.class);
        assertThat(authority.getAuthority()).isEqualTo("USER");
    }

    @Test
    @DisplayName("isAccountNonExpired should return true")
    void testIsAccountNonExpired() {
        assertThat(userPrincipal.isAccountNonExpired()).isTrue();
    }

    @Test
    @DisplayName("isAccountNonLocked should return true")
    void testIsAccountNonLocked() {
        assertThat(userPrincipal.isAccountNonLocked()).isTrue();
    }

    @Test
    @DisplayName("isCredentialsNonExpired should return true")
    void testIsCredentialsNonExpired() {
        assertThat(userPrincipal.isCredentialsNonExpired()).isTrue();
    }

    @Test
    @DisplayName("isEnabled should return true")
    void testIsEnabled() {
        assertThat(userPrincipal.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("getUsername should return the email")
    void testGetUsername() {
        assertThat(userPrincipal.getUsername()).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("getPassword should return the password")
    void testGetPassword() {
        assertThat(userPrincipal.getPassword()).isEqualTo("securePassword123");
    }

    @Test
    @DisplayName("Verify that UserDAO methods are called exactly once during construction")
    void testUserDAOInteractions() {
        // Verify that each getter in UserDAO was called once
        verify(mockUserDAO, times(1)).getId();
        verify(mockUserDAO, times(1)).getFirstName();
        verify(mockUserDAO, times(1)).getLastName();
        verify(mockUserDAO, times(1)).getEmail();
        verify(mockUserDAO, times(1)).getPassword();
    }
}