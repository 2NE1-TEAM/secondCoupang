package com.toanyone.delivery.common.filter;

import com.toanyone.delivery.common.utils.UserContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Slf4j(topic = "CustomFilter")
@Order(1)
@Component
public class CustomFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        UserContext context = createUserContext(request);
        UserContext.setCurrentContext(context);
        handleRequest(request, response, filterChain, UserContext.getUserContext().getRole());
    }

    private void handleRequest(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain, String role) throws IOException, ServletException {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        if (canAccessDeliveryManagerPutDeletePost(requestURI, method, role) ||
                canAccessDeliveryManagerGet(requestURI, method, role) ||
                canAccessDeliveryDelete(requestURI, method, role) ||
                canAccessDeliveryPut(requestURI, method, role) ||
                canAccessDeliveryGet(requestURI, method, role)) {
            filterChain.doFilter(request, response);
            UserContext.clear();
            return;
        }
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        UserContext.clear();
    }

    private boolean canAccessDeliveryManagerPutDeletePost(String requestURI, String method, String role) {
        return requestURI.startsWith(PathConstants.DELIVERY_MANAGER_PATH) &&
                (method.equals(HttpMethod.PUT.name()) || method.equals(HttpMethod.DELETE.name()) || method.equals(HttpMethod.POST.name())) &&
                (role.equals(RoleConstants.MASTER) || role.equals(RoleConstants.HUB));
    }

    private boolean canAccessDeliveryManagerGet(String requestURI, String method, String role) {
        return requestURI.startsWith(PathConstants.DELIVERY_MANAGER_PATH) &&
                method.equals(HttpMethod.GET.name()) &&
                (role.equals(RoleConstants.MASTER) || role.equals(RoleConstants.HUB) || role.equals(RoleConstants.DELIVERY));
    }

    private boolean canAccessDeliveryDelete(String requestURI, String method, String role) {
        return requestURI.startsWith(PathConstants.DELIVERY_PATH) &&
                method.equals(HttpMethod.DELETE.name()) &&
                (role.equals(RoleConstants.MASTER) || role.equals(RoleConstants.HUB));
    }

    private boolean canAccessDeliveryPut(String requestURI, String method, String role) {
        return requestURI.startsWith(PathConstants.DELIVERY_PATH) &&
                method.equals(HttpMethod.PUT.name()) &&
                (role.equals(RoleConstants.MASTER) || role.equals(RoleConstants.HUB) || role.equals(RoleConstants.DELIVERY));
    }

    private boolean canAccessDeliveryGet(String requestURI, String method, String role) {
        return requestURI.startsWith(PathConstants.DELIVERY_PATH) &&
                method.equals(HttpMethod.GET.name()) &&
                (role.equals(RoleConstants.MASTER) || role.equals(RoleConstants.HUB) || role.equals(RoleConstants.DELIVERY) || role.equals(RoleConstants.STORE));
    }

    private String getRequiredHeader(HttpServletRequest request, String headerName) {
        return Optional.ofNullable(request.getHeader(headerName))
                .orElseThrow(() -> new IllegalArgumentException(headerName + " 이 존재하지 않습니다."));
    }

    private UserContext createUserContext(HttpServletRequest request) {
        return UserContext.builder()
                .userId(Long.parseLong(getRequiredHeader(request, HeaderConstants.USER_ID_HEADER)))
                .role(getRequiredHeader(request, HeaderConstants.USER_ROLES_HEADER))
                .slackId(getRequiredHeader(request, HeaderConstants.SLACK_ID_HEADER))
                .hubId(Long.parseLong(getRequiredHeader(request, HeaderConstants.HUB_ID_HEADER)))
                .build();
    }

    public static final class RoleConstants {
        public static final String MASTER = "MASTER";
        public static final String HUB = "HUB";
        public static final String DELIVERY = "DELIVERY";
        public static final String STORE = "STORE";

        private RoleConstants() {
        }
    }

    public static final class HeaderConstants {
        public static final String USER_ROLES_HEADER = "X-User-Roles";
        public static final String USER_ID_HEADER = "X-User-Id";
        public static final String SLACK_ID_HEADER = "X-Slack-Id";
        public static final String HUB_ID_HEADER = "X-Hub-Id";

        private HeaderConstants() {
        }
    }

    public static final class PathConstants {
        public static final String DELIVERY_MANAGER_PATH = "/deliveries/delivery-manager";
        public static final String DELIVERY_PATH = "/deliveries";

        private PathConstants() {}
    }

}
