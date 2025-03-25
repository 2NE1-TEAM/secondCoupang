package com.toanyone.hub.common.config.interceptor;

import com.toanyone.hub.common.filter.UserContext;
import com.toanyone.hub.presentation.dto.UserInfo;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserInfoHeaderInterceptor implements RequestInterceptor {

    private final UserContext userContext;

    @Override
    public void apply(RequestTemplate template) {
        try {
            UserInfo user = userContext.getUser();
            if (user != null) {
                template.header("X-User-Id", String.valueOf(user.getUserId()));
                template.header("X-User-Roles", user.getRole());
                template.header("X-Slack-Id", user.getSlackId());
            }
        } catch (Exception e) {
            // request scope가 없으면 아무것도 안 넣음
            // Kafka Consumer 등에서 발생
        }
    }
}