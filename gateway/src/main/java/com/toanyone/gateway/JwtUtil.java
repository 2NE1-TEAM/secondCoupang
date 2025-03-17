package com.toanyone.gateway;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import javax.crypto.SecretKey;

@Component
@Slf4j
public class JwtUtil {

    @Value("${service.jwt.secret-key}")
    private String secretKey;

    @Value("${service.jwt.issuer}")
    private String issuer;

    public String extractToken(ServerWebExchange exchange) {

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secretKey));
            Jws<Claims> claimsJws = Jwts.parser()
                    .verifyWith(key)
                    .build().parseSignedClaims(token);
//            log.info("#####payload :: " + claimsJws.getPayload().toString());

            // issuer 검증로직
            if(!issuer.equals(claimsJws.getBody().getIssuer())){
                return false;
            }
            return true;
        } catch (SignatureException e) {
            // 서명이 유효하지 않음
            throw new RuntimeException("Invalid JWT signature", e);
        } catch (ExpiredJwtException e) {
            // 토큰이 만료됨
            throw new RuntimeException("JWT token is expired", e);
        } catch (MalformedJwtException e) {
            // 토큰 형식이 잘못됨
            throw new RuntimeException("Invalid JWT token format", e);
        } catch (UnsupportedJwtException e) {
            // 지원되지 않는 JWT 형식
            throw new RuntimeException("Unsupported JWT token", e);
        } catch (IllegalArgumentException e) {
            // 토큰이 비어있거나 null
            throw new RuntimeException("JWT token is empty or null", e);
        } catch (Exception e) {
            // 기타 예외
            throw new RuntimeException("JWT validation failed", e);
        }
    }

    public Claims extractClaims(String token) {
        try {
            // JWT 파싱 및 검증
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();// Claims 추출

            return claims;

        } catch (SignatureException e) {
            // 서명이 유효하지 않음
            throw new RuntimeException("Invalid JWT signature", e);
        } catch (ExpiredJwtException e) {
            // 토큰이 만료됨
            throw new RuntimeException("JWT token is expired", e);
        } catch (MalformedJwtException e) {
            // 토큰 형식이 잘못됨
            throw new RuntimeException("Invalid JWT token format", e);
        } catch (UnsupportedJwtException e) {
            // 지원되지 않는 JWT 형식
            throw new RuntimeException("Unsupported JWT token", e);
        } catch (IllegalArgumentException e) {
            // 토큰이 비어있거나 null
            throw new RuntimeException("JWT token is empty or null", e);
        } catch (Exception e) {
            // 기타 예외
            throw new RuntimeException("JWT validation failed", e);
        }

    }
}
