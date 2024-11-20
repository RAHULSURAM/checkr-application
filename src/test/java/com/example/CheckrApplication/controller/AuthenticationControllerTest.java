package com.example.CheckrApplication.controller;

import com.example.CheckrApplication.DTO.*;
import com.example.CheckrApplication.exception.BadRequestException;
import com.example.CheckrApplication.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @InjectMocks
    private AuthenticationController authenticationController;

    @Mock
    private AuthenticationService authenticationService;

    private SignupRequestDTO signupRequestDTO;
    private SigninRequestDTO signinRequestDTO;
    private SigninResponseDTO signinResponseDTO;
    private ApiResponse apiResponse;

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

// Initialize SigninRequestDTO
        signinRequestDTO = new SigninRequestDTO();
        signinRequestDTO.setEmail("test@example.com");
        signinRequestDTO.setPassword("password123");

// Initialize SigninResponseDTO
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setEmail("test@example.com");
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");
        userDTO.setRole("USER");

        signinResponseDTO = new SigninResponseDTO();
        signinResponseDTO.setToken("fake-jwt-token");
        signinResponseDTO.setUser(userDTO);

// Initialize ApiResponse
        apiResponse = new ApiResponse(true, "Created");
    }

    @Test
    void registerUser_Success() throws BadRequestException {
        // Arrange
        doNothing().when(authenticationService).registerUser(signupRequestDTO);

// Act
        ResponseEntity<?> response = authenticationController.registerUser(signupRequestDTO);
        ApiResponse responseBody = (ApiResponse) response.getBody();

// Assert
        assertEquals(201, response.getStatusCodeValue());
        assertNotNull(responseBody);
        assertEquals(apiResponse.isSuccess(), responseBody.isSuccess());
        verify(authenticationService, times(1)).registerUser(signupRequestDTO);
    }

    @Test
    void registerUser_BadRequestException() throws BadRequestException {
        // Arrange
        doThrow(new BadRequestException("Email is already in use")).when(authenticationService).registerUser(signupRequestDTO);

// Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            authenticationController.registerUser(signupRequestDTO);
        });

        assertEquals("Email is already in use", exception.getMessage());
        verify(authenticationService, times(1)).registerUser(signupRequestDTO);
    }

    @Test
    void authenticateUser_Success() {
        // Arrange
        when(authenticationService.authenticateUser(signinRequestDTO)).thenReturn(signinResponseDTO);

// Act
        ResponseEntity<SigninResponseDTO> response = authenticationController.authenticateUser(signinRequestDTO);

// Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(signinResponseDTO, response.getBody());
        verify(authenticationService, times(1)).authenticateUser(signinRequestDTO);
    }
}
