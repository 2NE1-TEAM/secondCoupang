package com.toanyone.hub.infrastructure.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.toanyone.hub.common.util.PhoneNumberUtils;
import com.toanyone.hub.domain.model.Hub;
import com.toanyone.hub.domain.model.QHub;
import com.toanyone.hub.presentation.dto.CursorInfo;
import com.toanyone.hub.presentation.dto.CursorPage;
import com.toanyone.hub.presentation.dto.HubFindResponseDto;
import com.toanyone.hub.presentation.dto.HubSearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class HubRepositoryImpl implements HubRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public CursorPage search(HubSearchRequest hubSearchRequest, String sortBy, String direction, int size) {
        QHub hub = QHub.hub;

        List<Hub> results = queryFactory
                .selectFrom(hub)
                .where(
                        keywordContains(hubSearchRequest.getKeyword()),
                        telephoneContains(hubSearchRequest.getTelephone()),
                        cursorBasedPaging(hubSearchRequest.getLastHubId(), hubSearchRequest.getLastCreatedAt(), hubSearchRequest.getLastHubName(), sortBy, direction)
                )
                .orderBy(getSortCondition(sortBy, direction)) // 다중 정렬 적용
                .limit(size + 1)  // 다음 페이지 확인을 위해 +1 조회
                .fetch();

        // 다음 페이지 존재 여부 확인
        boolean hasNext = results.size() > size;
        CursorInfo nextCursorInfo = null;

        if (hasNext) {
            Hub lastHub = results.remove(results.size() - 1);  // 마지막 데이터 제거하면서 반환

            // 정렬 기준에 따라 커서 정보를 생성
            if ("createdAt".equals(sortBy)) {
                nextCursorInfo = new CursorInfo(lastHub.getId(), lastHub.getCreatedAt(), null);
            } else if ("hubName".equals(sortBy)) {
                nextCursorInfo = new CursorInfo(lastHub.getId(), null, lastHub.getHubName());
            }
        }

        return new CursorPage<>(results.stream().map(h ->
                new HubFindResponseDto(h.getId(), h.getHubName(),
                        h.getAddress(), h.getLocation(), h.getTelephone())).toList(), nextCursorInfo, hasNext);
    }

    // 전화번호 검색
    private Predicate telephoneContains(String telephone) {
        return StringUtils.hasText(telephone) ? QHub.hub.telephone.contains(PhoneNumberUtils.normalizePhoneNumber(telephone)) : null;
    }

    // 허브 이름 검색
    private BooleanExpression keywordContains(String keyword) {
        return StringUtils.hasText(keyword) ? QHub.hub.hubName.containsIgnoreCase(keyword) : null;
    }

    private BooleanExpression cursorBasedPaging(Long cursorId, LocalDateTime cursorCreatedAt, String cursorHubName, String sortBy, String direction) {
        if (cursorId == null) { //cursorId가 null이면 첫페이지므로 null 반환.
            return null;
        }

        QHub hub = QHub.hub;
        boolean isDescending = "DESC".equalsIgnoreCase(direction);

        if ("createdAt".equals(sortBy)) {
            return isDescending
                    ? hub.createdAt.lt(cursorCreatedAt)
                    .or(hub.createdAt.eq(cursorCreatedAt).and(hub.id.loe(cursorId)))
                    : hub.createdAt.gt(cursorCreatedAt)
                    .or(hub.createdAt.eq(cursorCreatedAt).and(hub.id.goe(cursorId)));
        }

        if ("hubName".equals(sortBy)) {
            return isDescending
                    ? hub.hubName.lt(cursorHubName)
                    .or(hub.hubName.eq(cursorHubName).and(hub.id.loe(cursorId)))
                    : hub.hubName.gt(cursorHubName)
                    .or(hub.hubName.eq(cursorHubName).and(hub.id.goe(cursorId)));
        }

        return null;
    }

    ///  정렬 기준 설정
    private OrderSpecifier<?>[] getSortCondition(String sortBy, String direction) {
        QHub hub = QHub.hub;
        boolean isAscending = "ASC".equalsIgnoreCase(direction);

        // 기본 정렬 기준: ID 내림차순
        OrderSpecifier<?> primarySort;
        OrderSpecifier<?> secondarySort = isAscending ? hub.id.asc() : hub.id.desc();

        if ("createdAt".equals(sortBy)) {
            primarySort = isAscending ? hub.createdAt.asc().nullsLast() : hub.createdAt.desc().nullsFirst();
        } else if ("hubName".equals(sortBy)) {
            primarySort = isAscending ? hub.hubName.asc().nullsLast() : hub.hubName.desc().nullsFirst();
        } else {
            // 기본 정렬이 없을 경우, ID 기준 내림차순 정렬
            primarySort = hub.id.desc();
        }

        return new OrderSpecifier[]{primarySort, secondarySort};
    }
}