package com.toanyone.hub.common.config.AuditorConfig;

import com.toanyone.hub.common.filter.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class getCurrentAuditor implements AuditorAware<Long> {
    private final ObjectProvider<UserContext> userContextProvider;

    @Override
    public Optional<Long> getCurrentAuditor() {
        // 요청 스코프가 활성화되어 있는 경우만 userContext 사용
        if (RequestContextHolder.getRequestAttributes() == null) {
            return Optional.of(-1L); // system 또는 default
        }

        UserContext userContext = userContextProvider.getIfAvailable();
        if (userContext == null || userContext.getUser() == null) {
            return Optional.of(-1L);
        }
        return Optional.ofNullable(userContext.getUser().getUserId());
    }
}