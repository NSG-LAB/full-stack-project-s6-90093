package com.example.security;

<<<<<<< HEAD
import com.example.config.JwtProperties;
import com.example.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
=======
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

>>>>>>> copilot/worktree-2026-04-06T05-00-30
import java.util.Date;

@Component
public class JwtUtil {

<<<<<<< HEAD
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_ROLE = "role";

    private final Key secretKey;
    private final long expirationMs;

    public JwtUtil(JwtProperties jwtProperties) {
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes());
        this.expirationMs = jwtProperties.expirationMs();
    }
=======
    private final String SECRET_KEY = "secret";
>>>>>>> copilot/worktree-2026-04-06T05-00-30

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
<<<<<<< HEAD
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey)
                .compact();
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .claim(CLAIM_USER_ID, user.getId())
                .claim(CLAIM_EMAIL, user.getEmail())
                .claim(CLAIM_ROLE, user.getRole())
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey)
=======
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
>>>>>>> copilot/worktree-2026-04-06T05-00-30
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

<<<<<<< HEAD
    public String extractEmail(String token) {
        Claims claims = extractAllClaims(token);
        Object email = claims.get(CLAIM_EMAIL);
        return email != null ? email.toString() : claims.getSubject();
    }

    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        Object userId = claims.get(CLAIM_USER_ID);
        if (userId instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        Object role = claims.get(CLAIM_ROLE);
        return role != null ? role.toString() : null;
    }

    public boolean isTokenValid(String token, User user) {
        String email = extractEmail(token);
        return email.equals(user.getEmail()) && !isTokenExpired(token);
    }

=======
>>>>>>> copilot/worktree-2026-04-06T05-00-30
    public boolean isTokenValid(String token, String username) {
        return extractUsername(token).equals(username) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
<<<<<<< HEAD
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
=======
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
>>>>>>> copilot/worktree-2026-04-06T05-00-30
    }
}