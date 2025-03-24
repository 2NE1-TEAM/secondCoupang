package com.toanyone.delivery.common.utils;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UserContext {

    private static final ThreadLocal<UserContext> userContext = ThreadLocal.withInitial(UserContext::new);

    private Long userId;
    private String role;
    private String slackId;
    private Long hubId;

    @Builder
    public UserContext(Long userId, String role, String slackId, Long hubId) {
        this.userId = userId;
        this.role = role;
        this.slackId = slackId;
        this.hubId = hubId;
    }

    public UserContext() {
    }

    public static UserContext getUserContext() {
        return userContext.get();
    }

    public static void setCurrentContext(UserContext context) {
        userContext.set(context);
    }

    public static void clear() {
        userContext.remove();
    }
}
