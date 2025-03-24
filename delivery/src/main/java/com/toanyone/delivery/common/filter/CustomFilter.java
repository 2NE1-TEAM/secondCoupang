package com.toanyone.delivery.common.filter;

import com.toanyone.delivery.common.utils.UserContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Slf4j(topic = "CustomFilter")
@Order(1)
@Component
public class CustomFilter implements Filter {

    private static final String USER_ROLES_HEADER = "X-User-Roles";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String SLACK_ID_HEADER = "X-Slack-Id";
    private static final String HUB_ID_HEADER = "X-Hub-Id";
    private static final String DELIVERY_MANAGER_PATH = "/deliveries/delivery-manager/";
    private static final String DELIVERY_PATH = "/deliveries/";


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        log.info("doFilter");

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        Optional<String> role = Optional.ofNullable(request.getHeader(USER_ROLES_HEADER));
        Optional<String> userId = Optional.ofNullable(request.getHeader(USER_ID_HEADER));
        Optional<String> slackId = Optional.ofNullable(request.getHeader(SLACK_ID_HEADER));
        Optional<String> hubId = Optional.ofNullable(request.getHeader(HUB_ID_HEADER));

        if (role.isEmpty()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        UserContext context = UserContext.builder()
                .userId(Long.valueOf(userId.get()))
                .role(role.get())
                .slackId(slackId.get())
                .hubId(Long.valueOf(hubId.get()))
                .build();

        UserContext.setCurrentContext(context);

        String requestURI = request.getRequestURI();

        if (requestURI.startsWith(DELIVERY_MANAGER_PATH) && (request.getMethod().equals("PUT") || request.getMethod().equals("DELETE") || request.getMethod().equals("POST"))) {
            if (role.get().equals("MASTER") || role.get().equals("HUB")) {
                filterChain.doFilter(request, response);
            }
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        }

        if (requestURI.startsWith(DELIVERY_MANAGER_PATH) && (request.getMethod().equals("GET"))) {
            if (role.get().equals("MASTER") || role.get().equals("HUB") || role.get().equals("DELIVERY")) {
                filterChain.doFilter(request, response);
            }
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        }

        if (requestURI.startsWith(DELIVERY_PATH) && (request.getMethod().equals("DELETE"))) {
            if (role.get().equals("MASTER") || role.get().equals("HUB")) {
                filterChain.doFilter(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        }


        if (requestURI.startsWith(DELIVERY_PATH) && (request.getMethod().equals("PUT"))) {
            if (role.get().equals("MASTER") || role.get().equals("HUB") || role.get().equals("DELIVERY")) {
                filterChain.doFilter(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        }

        if (requestURI.startsWith(DELIVERY_PATH) && (request.getMethod().equals("GET"))) {
            if (role.get().equals("MASTER") || role.get().equals("HUB") || role.get().equals("DELIVERY") || role.get().equals("STORE")) {
                filterChain.doFilter(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        }

        UserContext.clear();

    }
}
