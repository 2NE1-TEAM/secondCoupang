package com.toanyone.order.common;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j(topic = "CustomFilter")
@Order(1)
@Component
public class CustomFilter extends OncePerRequestFilter {

    @Override
    public void doFilterInternal(HttpServletRequest servletRequest, HttpServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        log.info("doFilter");

        String userIdHeader = servletRequest.getHeader("User-Id");
        String userRoleHeader = servletRequest.getHeader("User-Role");
        String slackIdHeader = servletRequest.getHeader("Slack-Id");

        UserContext context = UserContext.builder()
                .userId(Long.parseLong(userIdHeader))
                .role(userRoleHeader)
                .slackId(Long.parseLong(slackIdHeader))
                .build();

        UserContext.setUserContext(context);

        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {

            log.info("clear ThreadLocal");
            UserContext.clear();
        }

    }
}