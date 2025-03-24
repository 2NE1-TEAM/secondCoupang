package com.toanyone.store.common.config.interceptor;

import com.toanyone.store.common.filter.UserContext;
import com.toanyone.store.presentation.dto.UserInfo;
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
        UserInfo user = userContext.getUser();
        if (user != null) {
            template.header("X-User-Id", String.valueOf(user.getUserId()));
            template.header("X-User-Roles", user.getRole());
            template.header("X-Slack-Id", user.getSlackId());
        }
    }
}