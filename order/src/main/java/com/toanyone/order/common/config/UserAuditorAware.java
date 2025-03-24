package com.toanyone.order.common.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

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