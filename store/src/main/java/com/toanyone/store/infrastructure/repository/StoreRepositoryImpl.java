package com.toanyone.store.infrastructure.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.toanyone.store.common.util.PhoneNumberUtils;
import com.toanyone.store.domain.model.QStore;
import com.toanyone.store.domain.model.Store;
import com.toanyone.store.presentation.dto.CursorInfo;
import com.toanyone.store.presentation.dto.CursorPage;
import com.toanyone.store.presentation.dto.StoreSearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public CursorPage<Store> search(StoreSearchRequest storeSearchRequest, String sortBy, String direction, int size) {
        QStore store = QStore.store;

        List<Store> results = queryFactory
                .selectFrom(store)
                .where(
                        keywordContains(storeSearchRequest.getKeyword()),
                        hubIdEq(storeSearchRequest.getHubId()),
                        telephoneContains(storeSearchRequest.getTelephone()),
                        cursorBasedPaging(storeSearchRequest.getLastStoreId(), storeSearchRequest.getLastCreatedAt(), storeSearchRequest.getLastStoreName(), sortBy, direction)
                )
                .orderBy(getSortCondition(sortBy, direction)) // 다중 정렬 적용
                .limit(size + 1)  // 다음 페이지 확인을 위해 +1 조회
                .fetch();

        // 다음 페이지 존재 여부 확인
        boolean hasNext = results.size() > size;
        CursorInfo nextCursorInfo = null;

        if (hasNext) {
            Store lastStore = results.remove(results.size() - 1);  // 마지막 데이터 제거하면서 반환

            // 정렬 기준에 따라 커서 정보를 생성
            if ("createdAt".equals(sortBy)) {
                nextCursorInfo = new CursorInfo(lastStore.getId(), lastStore.getCreatedAt(), null);
            } else if ("storeName".equals(sortBy)) {
                nextCursorInfo = new CursorInfo(lastStore.getId(), null, lastStore.getStoreName());
            }
        }
        return new CursorPage<>(results, nextCursorInfo, hasNext);
    }

    // 전화번호 검색
    private Predicate telephoneContains(String telephone) {
        return StringUtils.hasText(telephone) ? QStore.store.telephone.contains(PhoneNumberUtils.normalizePhoneNumber(telephone)) : null;
    }

    // 가게 이름 검색
    private BooleanExpression keywordContains(String keyword) {
        return StringUtils.hasText(keyword) ? QStore.store.storeName.containsIgnoreCase(keyword) : null;
    }

    // 허브 ID 필터링
    private BooleanExpression hubIdEq(Long hubId) {
        return hubId != null ? QStore.store.hubId.eq(hubId) : null;
    }

    private BooleanExpression cursorBasedPaging(Long cursorId, LocalDateTime cursorCreatedAt, String cursorStoreName, String sortBy, String direction) {
        if (cursorId == null) { //cursorId가 null이면 첫페이지므로 null 반환.
            return null;
        }

        QStore store = QStore.store;
        boolean isDescending = "DESC".equalsIgnoreCase(direction);

        // createdAt 기준 페이징
        if ("createdAt".equals(sortBy)) {
            return isDescending
                    ? store.createdAt.lt(cursorCreatedAt).or(store.createdAt.eq(cursorCreatedAt).and(store.id.lt(cursorId)))
                    : store.createdAt.gt(cursorCreatedAt).or(store.createdAt.eq(cursorCreatedAt).and(store.id.gt(cursorId)));
        }

        // storeName 기준 페이징
        if ("storeName".equals(sortBy)) {
            return isDescending
                    ? store.storeName.lt(cursorStoreName).or(store.storeName.eq(cursorStoreName).and(store.id.lt(cursorId)))
                    : store.storeName.gt(cursorStoreName).or(store.storeName.eq(cursorStoreName).and(store.id.gt(cursorId)));
        }

        return null;
    }

    ///  정렬 기준 설정
    private OrderSpecifier<?>[] getSortCondition(String sortBy, String direction) {
        QStore store = QStore.store;
        boolean isAscending = "ASC".equalsIgnoreCase(direction);

        // 기본 정렬 기준: ID 내림차순
        OrderSpecifier<?> primarySort;
        OrderSpecifier<?> secondarySort = isAscending ? store.id.asc() : store.id.desc();

        if ("createdAt".equals(sortBy)) {
            primarySort = isAscending ? store.createdAt.asc().nullsLast() : store.createdAt.desc().nullsFirst();
        } else if ("storeName".equals(sortBy)) {
            primarySort = isAscending ? store.storeName.asc().nullsLast() : store.storeName.desc().nullsFirst();
        } else {
            // 기본 정렬이 없을 경우, ID 기준 내림차순 정렬
            primarySort = store.id.desc();
        }

        return new OrderSpecifier[]{primarySort, secondarySort};
    }
}