package com.backend.SecureSharing.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    private long accessTokenExpiration = 1000 * 60 * 15; // 15 minutes
    private long refreshTokenExpiration = 1000L * 60 * 60 * 24 * 7; // 7 days

    // ------------------ TOKEN GENERATION ------------------
    public String generateAccessToken(String username) {
        return generateToken(username, accessTokenExpiration);
    }

    public String generateRefreshToken(String username) {
        return generateToken(username, refreshTokenExpiration);
    }

    private String generateToken(String username, long expirationTime) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSignInKey())
                .compact();
    }

    // ------------------ TOKEN VALIDATION ------------------
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String userEmail = extractUsername(token);

        if (isTokenExpired(token)) {
            System.out.println("❌ Token is expired for user: " + userEmail);
            return false;
        }

        if (!userEmail.equalsIgnoreCase(userDetails.getUsername())) {
            System.out.println("❌ JWT subject (" + userEmail + ") does not match UserDetails username (" + userDetails.getUsername() + ")");
            return false;
        }

        System.out.println("✅ Token is valid for user: " + userEmail);
        return true;
    }

    // ------------------ CLAIMS EXTRACTION ------------------
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ------------------ KEY ------------------
    private Key getSignInKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
