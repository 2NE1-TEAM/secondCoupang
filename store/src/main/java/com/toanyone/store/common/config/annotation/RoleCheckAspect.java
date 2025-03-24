package com.toanyone.store.common.config.annotation;

import com.toanyone.store.common.filter.UserContext;
import com.toanyone.store.domain.exception.StoreException;
import com.toanyone.store.presentation.dto.UserInfo;
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
            throw new StoreException.StoreDeniedException("인증 정보가 없습니다.");
        }

        boolean hasRole = Arrays.stream(requireRole.value())
                .anyMatch(role -> role.equals(user.getRole()));

        if (!hasRole) {
            throw new StoreException.StoreDeniedException("해당 요청에 대한 권한이 없습니다.");
        }
    }
}