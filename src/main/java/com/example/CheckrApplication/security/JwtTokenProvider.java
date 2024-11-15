package com.example.CheckrApplication.security;


import io.jsonwebtoken.Claims;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtTokenProvider {

    private String secretkey = "";

    public JwtTokenProvider() {

        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
            SecretKey sk = keyGen.generateKey();
            secretkey = Base64.getEncoder().encodeToString(sk.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
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
        byte[] keyBytes = Decoders.BASE64.decode(secretkey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUserName(String token) {
        // extract the username from jwt token
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String userName = extractUserName(token);
        return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

//    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
//
//
//
//
//    private final JwtConfig jwtConfig = new JwtConfig();
//
//
//
//    private  String secret="";
//
//    private final Key key=initializeKey(secret);
//
//
//
//
//    public JwtTokenProvider(){
//        long expirationMs = 86400000;
//        jwtConfig.setExpirationMs(expirationMs);
//        jwtConfig.setSecret(secret);
//    }
//
////    public JwtTokenProvider(JwtConfig jwtConfig) {
////        this.jwtConfig = jwtConfig;
////        this.key = initializeKey(jwtConfig.getSecret());
////    }
//
//    private Key initializeKey(String secret) {
//        try {
//            // Decode the Base64-encoded secret key
//            byte[] decodedKey =  Decoders.BASE64.decode(secret);
//            return Keys.hmacShaKeyFor(decodedKey);
//        } catch (IllegalArgumentException e) {
//            logger.error("Failed to decode JWT secret key: {}", e.getMessage());
//            throw new RuntimeException("Invalid JWT secret key.");
//        }
//    }
//
//    /**
//     * Generates a JWT token for the authenticated user.
//     *
//     * @param authentication The authentication object containing user details.
//     * @return A signed JWT token as a String.
//     */
//    public String generateToken(Authentication authentication) {
//        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
//
//        Date now = new Date(System.currentTimeMillis());
//        Date expiryDate = new Date(now.getTime() + jwtConfig.getExpirationMs());
//
//        return Jwts.builder()
//                .subject(Long.toString(userPrincipal.getId()))
//                .issuedAt(now)
//                .expiration(expiryDate)
//                .signWith(key) // Updated signWith method
//                .compact();
//    }
//
//    /**
//     * Extracts the user ID from the JWT token.
//     *
//     * @param token The JWT token.
//     * @return The user ID as a Long.
//     */
//    public Long getUserIdFromJWT(String token) {
//        try {
//            Claims claims = Jwts.parser() // Updated parser method
//                    .verifyWith((SecretKey) key).build()
//                    .parseSignedClaims(token)
//                    .getPayload();
//
//            return Long.parseLong(claims.getSubject());
//        } catch (JwtException ex) {
//            logger.error("Failed to parse JWT token: {}", ex.getMessage());
//            return null;
//        }
//    }
//
//    /**
//     * Validates the JWT token.
//     *
//     * @param authToken The JWT token to validate.
//     * @return true if valid, false otherwise.
//     */
//    public boolean validateToken(String authToken) {
//        try {
//            Jwts.parser() // Updated parser method
//                    .verifyWith((SecretKey) key).build()
//                    .parseSignedClaims(authToken);
//            return true;
//        } catch (SecurityException | MalformedJwtException ex) {
//            // Log invalid JWT signature
//            logger.error("Invalid JWT signature: {}", ex.getMessage());
//        } catch (ExpiredJwtException ex) {
//            // Log expired JWT token
//            logger.error("Expired JWT token: {}", ex.getMessage());
//        } catch (UnsupportedJwtException ex) {
//            // Log unsupported JWT token
//            logger.error("Unsupported JWT token: {}", ex.getMessage());
//        } catch (IllegalArgumentException ex) {
//            // Log empty JWT claims
//            logger.error("JWT claims string is empty: {}", ex.getMessage());
//        }
//        return false;
//    }
}