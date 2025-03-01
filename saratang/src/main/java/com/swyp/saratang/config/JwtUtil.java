package com.swyp.saratang.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key secretKey;

    @Value("${jwt.expiration}")
    private long expirationTime;

    /**
     *  생성자에서 시크릿 키를 Base64 디코딩 후 Key 객체로 변환
     */
    public JwtUtil(@Value("${jwt.secret}") String secret) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("JWT 시크릿 키가 너무 짧습니다. 256비트(32바이트) 이상이어야 합니다.");
        }

        this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
    }

    /**
     *  JWT 토큰 생성
     */
    public String generateToken(String userId, String email, String provider) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("email", email)
                .claim("provider", provider)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     *  JWT 토큰에서 사용자 정보 추출
     */
    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     *  JWT 토큰 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("JWT 만료됨");
        } catch (JwtException e) {
            System.out.println("JWT 검증 실패");
        }
        return false;
    }
}