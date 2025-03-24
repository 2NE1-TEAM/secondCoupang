package com.toanyone.hub.common.config.AuditorConfig;

import com.toanyone.hub.common.filter.UserContext;
import com.toanyone.hub.presentation.dto.UserInfo;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class getCurrentAuditor implements AuditorAware<Long> {
    @Override
    public Optional<Long> getCurrentAuditor() {
        return Optional.ofNullable(UserContext.getUser())
                .map(UserInfo::getUserId)
                .or(() -> Optional.of(-1L)); // 기본값
    }
}