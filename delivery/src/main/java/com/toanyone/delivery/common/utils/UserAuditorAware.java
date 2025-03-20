package com.toanyone.delivery.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class UserAuditorAware implements AuditorAware<Long> {
    @Override
    public Optional<Long> getCurrentAuditor() {
        UserContext userContext = UserContext.getUserContext();
        if (userContext == null) {
            return Optional.empty();
        }
        return Optional.of(userContext.getUserId());
    }
}