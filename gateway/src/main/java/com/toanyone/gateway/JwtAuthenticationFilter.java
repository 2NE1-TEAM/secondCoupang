package com.toanyone.gateway;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;

    @Value("${service.jwt.refresh}")
    private String refresh;

    @Value("${service.jwt.black-list}")
    private String blacklist;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if(path.equals("/users/sign-up") || path.equals("/users/sign-in")){
            return chain.filter(exchange);
        }

        if(path.equals("/users/logout") && exchange.getRequest().getMethod().equals(HttpMethod.POST)){
            logout(exchange);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return chain.filter(exchange);
        }

        String token = jwtUtil.extractToken(exchange);

        if(token == null || !jwtUtil.validateToken(token)){
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        Claims claims = jwtUtil.extractClaims(token);
        ServerWebExchange serverWebExchange = modifiedExchange(claims, exchange);

        return chain.filter(serverWebExchange);
    }

    private void logout(ServerWebExchange exchange) {
        String token = jwtUtil.extractToken(exchange);

        if(token == null || !jwtUtil.validateToken(token)){
            throw new RuntimeException("token이 존재하지 않습니다. ");
        }

        Claims claims = jwtUtil.extractClaims(token);
        Long userId = claims.get("userId", Long.class);
        redisTemplate.opsForSet().add(blacklist + ":" +  userId, token);
        redisTemplate.expire(blacklist + ":" +  userId, 60, TimeUnit.MINUTES);
        redisTemplate.delete(refresh + ":" + userId);
    }

    private ServerWebExchange modifiedExchange(Claims claims, ServerWebExchange exchange) {

        String userId = String.valueOf(claims.get("userId", Long.class));
        String userRole = claims.get("userRole", String.class);
        String slackId = claims.get("slackId", String.class);
        String nickName = claims.get("nickName", String.class);
        String hubId = String.valueOf(claims.get("hubId", Long.class));

        return exchange.mutate()
                .request(request -> request
                        .header("X-User-Id", userId)
                        .header("X-User-Roles", userRole)
                        .header("X-Slack-Id", slackId)
                        .header("X-Hub-Id", hubId)
                        .header("X-Nick-Name", nickName))
                .build();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
