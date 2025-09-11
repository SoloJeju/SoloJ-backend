
package com.dataury.soloJ.global.security;


import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.global.code.status.ErrorStatus;
import com.dataury.soloJ.global.exception.GeneralException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Component
@Log4j2
public class TokenProvider {

    @Value("${jwt.secret}")
    private String secretString;
    
    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;
    
    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;
    
    private SecretKey SECRET_KEY;
    
    @PostConstruct
    public void init() {
        log.info("✅ Loaded JWT_SECRET: '{}'", secretString);
        byte[] keyBytes = secretString.getBytes();
        this.SECRET_KEY = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .setExpiration(Date.from(Instant.now().plusSeconds(accessTokenExpiration)))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }


    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .setExpiration(Date.from(Instant.now().plusSeconds(refreshTokenExpiration)))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    @Deprecated
    public Claims parseToken(String token) {
        return extractClaims(token);
    }



    // 토큰 검증 및 클레임 추출
    public Claims extractClaims(String token) {
        try {
            String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(jwtToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // 만료된 토큰에서도 클레임 추출
            return e.getClaims();
        }
    }

    public String extractUserRoleFromToken(String token) {
        return extractClaims(token).get("role", String.class);
    }



    // 사용자 ID 추출
    public Long extractUserIdFromToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("토큰이 비어 있거나 null입니다.");
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Claims claims = extractClaims(token);
        log.info("JWT Claims = {}", claims);


        try {
            Object rawUserId = extractClaims(token).get("userId");
            if (rawUserId instanceof Integer) {
                return ((Integer) rawUserId).longValue();
            } else if (rawUserId instanceof Long) {
                return (Long) rawUserId;
            } else if (rawUserId instanceof String) {
                return Long.parseLong((String) rawUserId);
            } else {
                throw new GeneralException(ErrorStatus.JWT_MALFORMED);
            }
        } catch (Exception e) {
            throw new GeneralException(ErrorStatus.JWT_MALFORMED);
        }
    }





    // 토큰 유효성 확인
    public boolean isValidToken(String token) {
        try {
            String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(jwtToken);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}