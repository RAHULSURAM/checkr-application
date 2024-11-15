package com.example.CheckrApplication.security;


import com.example.CheckrApplication.DAO.UserDAO;
import com.example.CheckrApplication.JPARepository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    // Load user by username or email
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail)
            throws UsernameNotFoundException {
        Optional<UserDAO> userOpt = userRepository.findByEmail(usernameOrEmail);
        UserDAO user = userOpt.orElseThrow(() ->
                new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail)
        );
        return new UserPrincipal(user);
    }

    // Load user by ID (for JWT token)
    @Transactional
    public UserDetails loadUserById(Long id) {
        UserDAO user = userRepository.findById(id).orElseThrow(
                () -> new UsernameNotFoundException("User not found with id: " + id));
        return new UserPrincipal(user);
    }
}
