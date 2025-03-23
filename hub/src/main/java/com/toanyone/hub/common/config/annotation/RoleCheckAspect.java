package com.toanyone.hub.common.config.annotation;

import com.toanyone.hub.common.filter.UserContext;
import com.toanyone.hub.domain.exception.HubException;
import com.toanyone.hub.presentation.dto.UserInfo;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class RoleCheckAspect {

    private final UserContext userContext;

    @Before("@annotation(requireRole)")
    public void checkRole(RequireRole requireRole) {
        UserInfo user = userContext.getUser();
        if (user == null) {
            throw new HubException.HubDeniedException("인증 정보가 없습니다.");
        }

        boolean hasRole = Arrays.stream(requireRole.value())
                .anyMatch(role -> role.equals(user.getRole()));

        if (!hasRole) {
            throw new HubException.HubDeniedException("해당 요청에 대한 권한이 없습니다.");
        }
    }
}