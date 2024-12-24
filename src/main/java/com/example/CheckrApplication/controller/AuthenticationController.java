package com.example.CheckrApplication.controller;

import com.example.CheckrApplication.DTO.ApiResponse;
import com.example.CheckrApplication.DTO.SigninRequestDTO;
import com.example.CheckrApplication.DTO.SigninResponseDTO;
import com.example.CheckrApplication.DTO.SignupRequestDTO;
import com.example.CheckrApplication.exception.BadRequestException;
import com.example.CheckrApplication.service.AuthenticationService;
import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/checkr/auth/v1")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequestDTO signupRequestDTO) throws BadRequestException {
        log.info("Started /signup in AuthenticationController.registerUser");
        authenticationService.registerUser(signupRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, "User registered successfully"));
    }

    @PostMapping("/signin")
    public ResponseEntity<SigninResponseDTO> authenticateUser(@Valid @RequestBody SigninRequestDTO signinRequestDTO){
        log.info("Started /signin in AuthenticationController.authenticateUser");
        SigninResponseDTO response = authenticationService.authenticateUser(signinRequestDTO);
        return ResponseEntity.ok(response);
    }

//    @PostMapping("/reset-password")
//    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request){
//        authenticationService.resetPassword(request);
//        return ResponseEntity.ok(new ApiResponse(true, "Password reset link sent to email"));
//    }
//
//    @PostMapping("/confirm-reset-password")
//    public ResponseEntity<?> confirmResetPassword(@Valid @RequestBody ConfirmResetPasswordRequest request){
//        authenticationService.confirmResetPassword(request);
//        return ResponseEntity.ok(new ApiResponse(true, "Password has been reset successfully"));
//    }
}
