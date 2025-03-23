package com.toanyone.store.common.filter;

import com.toanyone.store.presentation.dto.UserInfo;

public class UserContext {
    private static final ThreadLocal<UserInfo> userThreadLocal = new ThreadLocal<>();

    public static void setUser(UserInfo user) {
        userThreadLocal.set(user);
    }

    public static UserInfo getUser() {
        return userThreadLocal.get();
    }

    public static void clear() {
        userThreadLocal.remove();
    }
}