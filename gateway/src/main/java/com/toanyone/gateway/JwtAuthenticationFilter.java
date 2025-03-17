package com.toanyone.gateway;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if(path.equals("/users/sign-up") || path.equals("/users/sign-in")){
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

    private ServerWebExchange modifiedExchange(Claims claims, ServerWebExchange exchange) {

        String userId = String.valueOf(claims.get("userId", Long.class));
        String userRole = claims.get("userRole", String.class);
        String slackId = claims.get("slackId", String.class);

        return exchange.mutate()
                .request(request -> request
                        .header("X-User-Id", userId)
                        .header("X-User-Roles", userRole)
                        .header("X-Slack-Id", slackId))
                .build();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
