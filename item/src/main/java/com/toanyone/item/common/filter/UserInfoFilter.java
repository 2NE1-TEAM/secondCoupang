package com.toanyone.item.common.filter;

import com.toanyone.item.presentation.dto.UserInfo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserInfoFilter extends OncePerRequestFilter {

    private final UserContext userContext; // DI 받음

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Optional<String> userIdHeaderStr = Optional.ofNullable(request.getHeader("X-User-Id"));
        Optional<String> roleHeader = Optional.ofNullable(request.getHeader("X-User-Roles"));
        Optional<String> slackHeader = Optional.ofNullable(request.getHeader("X-Slack-Id"));

        if (userIdHeaderStr.isEmpty() || roleHeader.isEmpty() || slackHeader.isEmpty()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "인증 정보가 없습니다.");
            return;
        }

        Long userIdHeader;
        try {
            userIdHeader = Long.valueOf(userIdHeaderStr.get());
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "유효하지 않은 userId 형식");
            return;
        }

        UserInfo userInfo = new UserInfo(userIdHeader, roleHeader.get(), slackHeader.get());
        userContext.setUser(userInfo);

        filterChain.doFilter(request, response);
    }
}