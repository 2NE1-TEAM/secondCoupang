package com.toanyone.store.common.config.AuditorConfig;

import com.toanyone.store.common.filter.UserContext;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class getCurrentAuditor implements AuditorAware<Long> {
    @Override
    public Optional<Long> getCurrentAuditor() {
        return Optional.of(UserContext.getUser().getUserId());
    }
}