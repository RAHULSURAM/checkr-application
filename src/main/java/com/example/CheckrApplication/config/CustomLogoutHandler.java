package com.example.CheckrApplication.config;

import com.example.CheckrApplication.DAO.Token;
import com.example.CheckrApplication.JPARepository.TokenRepository;
import com.example.CheckrApplication.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CustomLogoutHandler implements LogoutHandler {

    @Autowired
    private JwtTokenProvider jwtService;

    @Autowired
    private TokenRepository tokenRepository;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ") && authHeader.length() > 7) {
            token = authHeader.substring(7);
            username = jwtService.extractUserName(token);
        }
        else return;

        Optional<Token> logoutUserToken = tokenRepository.findByToken(token);
        if(logoutUserToken.isPresent()){
            logoutUserToken.get().setLoggedOut(true);
            tokenRepository.save(logoutUserToken.get());
        }
    }
}
