package com.toanyone.order.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toanyone.order.common.exception.OrderException;
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

        String userIdHeader = servletRequest.getHeader("X-User-Id");
        String userRoleHeader = servletRequest.getHeader("X-User-Role");
        String slackIdHeader = servletRequest.getHeader("X-Slack-Id");

        try {
            if (userIdHeader == null || userRoleHeader == null || slackIdHeader == null ||
                    userIdHeader.isEmpty() || userRoleHeader.isEmpty() || slackIdHeader.isEmpty()) {
                throw new OrderException.AuthenticationFailedException();
            }

            UserContext context = UserContext.builder()
                    .userId(Long.parseLong(userIdHeader))
                    .role(userRoleHeader)
                    .slackId(slackIdHeader)
                    .build();

            UserContext.setUserContext(context);
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (OrderException.AuthenticationFailedException e) {
            handleException(servletResponse, e);
        } finally {
            log.info("clear ThreadLocal");
            UserContext.clear();
        }

    }

    private void handleException(HttpServletResponse response, OrderException.AuthenticationFailedException e) throws IOException {
        log.error("Authentication Error: {}", e.getMessage());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        SingleResponse errorResponse = SingleResponse.error(e.getMessage(), e.getErrorCode());
        response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
    }

}
