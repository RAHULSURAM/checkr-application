package com.example.CheckrApplication.service;

import com.example.CheckrApplication.DAO.UserDAO;
import com.example.CheckrApplication.DTO.SigninRequestDTO;
import com.example.CheckrApplication.DTO.SigninResponseDTO;
import com.example.CheckrApplication.DTO.SignupRequestDTO;
import com.example.CheckrApplication.DTO.UserDTO;
import com.example.CheckrApplication.JPARepository.UserRepository;
import com.example.CheckrApplication.exception.BadRequestException;
import com.example.CheckrApplication.exception.ResourceNotFoundException;
import com.example.CheckrApplication.exception.InvalidCredentialsException;
import com.example.CheckrApplication.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;


    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    public void registerUser(SignupRequestDTO signupRequestDTO) throws BadRequestException {
        if(userRepository.existsByEmail(signupRequestDTO.getEmail())){
            throw new BadRequestException("Email is already in use");
        }

        if(!signupRequestDTO.getPassword().equals(signupRequestDTO.getConfirmPassword())){
            throw new BadRequestException("Passwords do not match");
        }

        UserDAO userDAO = new UserDAO();
        userDAO.setEmail(signupRequestDTO.getEmail());
        userDAO.setPassword(passwordEncoder.encode(signupRequestDTO.getPassword()));
        userDAO.setFirstName(signupRequestDTO.getFirstName());
        userDAO.setLastName(signupRequestDTO.getLastName());

        userRepository.save(userDAO);
    }

    public SigninResponseDTO authenticateUser(SigninRequestDTO signinRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        signinRequest.getEmail(),
                        signinRequest.getPassword()
                )
        );
        if(!authentication.isAuthenticated()) {
            throw new InvalidCredentialsException("Email or Password is wrong", signinRequest.getEmail());
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(signinRequest.getEmail());

        UserDAO user = userRepository.findByEmail(signinRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", signinRequest.getEmail()));

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setEmail(user.getEmail());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setRole(user.getRole().name());

        SigninResponseDTO response = new SigninResponseDTO();
        response.setToken(jwt);
        response.setUser(userDTO);

        return response;
    }

    // Other methods for reset password
}
