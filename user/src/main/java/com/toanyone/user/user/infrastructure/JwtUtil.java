package com.toanyone.user.user.infrastructure;

import com.toanyone.user.user.domain.UserRole;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    @Value("${spring.application.name}")
    private String issuer;

    @Value("${service.jwt.secret-key}")
    private String secretKey;

    private final Long ACCESS_EXPIRE_TIME = 60 * 60 * 1000L;
    private final Long REFRESH_EXPIRE_TIME = 7 * 60 * 60 * 1000L;
    private final String TOKEN_PREFIX = "Bearer ";



    public String generateAccessToken(Long userId, UserRole userRole, String slackId, Long hubId, String nickName, String phone) {

        return   TOKEN_PREFIX+ Jwts.builder()
                .claim("userId", userId)
                .claim("userRole", userRole)
                .claim("slackId", slackId)
                .claim("hubId", hubId)
                .claim("nickName", nickName)
                .claim("phone", phone)
                .issuer(issuer)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+ ACCESS_EXPIRE_TIME))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .claim("userId", userId)
                .issuer(issuer)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRE_TIME))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();

    }
}
