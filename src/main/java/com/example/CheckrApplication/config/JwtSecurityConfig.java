package com.example.CheckrApplication.config;

import com.example.CheckrApplication.exception.JwtAuthenticationEntryPoint;
import com.example.CheckrApplication.security.CustomUserDetailsService;
import com.example.CheckrApplication.security.JwtAuthenticationFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@Slf4j
public class JwtSecurityConfig {


    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @Autowired
    private CustomLogoutHandler logoutHandler;

    // Define the JWT authentication filter
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    // Password encoder bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager bean
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
            throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // Security filter chain
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("started security filter chain in jwtsecurityConfig");
        // Disable CSRF as we are using JWT
        http.csrf(csrf -> csrf.disable());


// Set session management to stateless
        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

// Set permissions on endpoints
        http.authorizeHttpRequests(auth -> auth
                // Allow access to auth endpoints and static resources
                .requestMatchers("/checkr/auth/**").permitAll()
                // All other endpoints require authentication
                .anyRequest().authenticated()
        );

// Set exception handling
        http.exceptionHandling(exception ->
                exception.authenticationEntryPoint(unauthorizedHandler));

// Allow frames for H2 console
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

// Add JWT filter before UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        //for using postman
        http.httpBasic(Customizer.withDefaults());

//for logout
        http.logout(l->l.logoutUrl("/logout")
                .addLogoutHandler(logoutHandler)
                .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext())
        );

        log.info("Ended security filter chain in jwtsecurityConfig");

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        log.info("started AuthenticationProvider in JwtSecurityConfig");
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(new BCryptPasswordEncoder(12));
        provider.setUserDetailsService(customUserDetailsService);

        log.info("Ended AuthenticationProvider in JwtSecurityConfig");


        return provider;
    }
}