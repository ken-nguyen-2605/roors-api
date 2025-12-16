package com.josephken.roors.auth.util;

import com.josephken.roors.auth.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${jwt.expiration}")
    private long jwtExpirationMs;
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        log.info("JWT Expiration Ms: {}", jwtExpirationMs);  // Add this line

        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtExpirationMs);

        log.info("Token issued at: {}, expires at: {}", now, expiration);  // Add this

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .claim("role", user.getRole())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

//    public String getUsernameFromToken(String token) {
//        return Jwts.parser().verifyWith(secretKey).build()
//                .parseSignedClaims(token)
//                .getPayload()
//                .getSubject();
//    }

    public Long getUserIdFromToken(String token) {
        String userIdStr = Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        return Long.parseLong(userIdStr);
    }

    public Collection<GrantedAuthority> getAuthoritiesFromToken(String token) {
        String role = Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    public boolean validateToken(String token) {
        Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
        return true;
    }
}
