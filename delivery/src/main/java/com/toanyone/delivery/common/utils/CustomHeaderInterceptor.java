package com.toanyone.delivery.common.utils;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

@Component
public class CustomHeaderInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        UserContext user = UserContext.getUserContext();

        if (user != null) {
            template.header("X-User-Id", String.valueOf(user.getUserId()));
            template.header("X-User-Roles", user.getRole());
            template.header("X-Slack-Id", user.getSlackId());
            if (user.getSlackId() != null) {
                template.header("X-Hub-Id", String.valueOf(user.getHubId()));
            }
        }
    }
}
