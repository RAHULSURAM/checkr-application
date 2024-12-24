package com.example.CheckrApplication.security;


import com.example.CheckrApplication.DAO.Token;
import com.example.CheckrApplication.JPARepository.TokenRepository;
import io.jsonwebtoken.Claims;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
public class JwtTokenProvider {

    @Autowired
    private TokenRepository tokenRepository;

    private String secretkey = "";

    public JwtTokenProvider() {

        log.info("started JwtTokenProvider in JwtTokenProvider");

        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
            SecretKey sk = keyGen.generateKey();
            secretkey = Base64.getEncoder().encodeToString(sk.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        log.info("Ended JwtTokenProvider in JwtTokenProvider");
    }

    public String generateToken(String username) {
        log.info("started generateToken in JwtTokenProvider");
        Map<String, Object> claims = new HashMap<>();
        log.info("Ended generateToken in JwtTokenProvider");
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 60 * 60 * 30 * 1000))
                .and()
                .signWith(getKey())
                .compact();


    }

    private SecretKey getKey() {
        log.info("started  and ended getKey in JwtTokenProvider");
        byte[] keyBytes = Decoders.BASE64.decode(secretkey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUserName(String token) {
        log.info("started  and ended extractUserName in JwtTokenProvider");
        // extract the username from jwt token
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        log.info("started  and ended extractClaim in JwtTokenProvider");
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        log.info("started  and ended extractAllClaims in JwtTokenProvider");
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        log.info("started  and ended validateToken in JwtTokenProvider");
        final String userName = extractUserName(token);
        final boolean validToken = tokenRepository.findByToken(token)
                .map(t->!t.isLoggedOut()).orElse(false);

        return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token) && validToken);
    }

    private boolean isTokenExpired(String token) {
        log.info("started  and ended isTokenExpired in JwtTokenProvider");
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        log.info("started  and ended extractExpiration in JwtTokenProvider");
        return extractClaim(token, Claims::getExpiration);
    }


}