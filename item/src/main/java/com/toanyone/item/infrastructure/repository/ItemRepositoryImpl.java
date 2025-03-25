package com.toanyone.item.infrastructure.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.toanyone.item.domain.model.Item;
import com.toanyone.item.domain.model.QItem;
import com.toanyone.item.presentation.dto.CursorInfo;
import com.toanyone.item.presentation.dto.CursorPage;
import com.toanyone.item.presentation.dto.ItemFindResponseDto;
import com.toanyone.item.presentation.dto.ItemSearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public CursorPage search(ItemSearchRequest itemSearchRequest, String sortBy, String direction, int size) {
        QItem item = QItem.item;

        List<Item> results = queryFactory
                .selectFrom(item)
                .where(
                        keywordContains(itemSearchRequest.getKeyword()),
                        cursorBasedPaging(itemSearchRequest.getLastItemId(), itemSearchRequest.getLastCreatedAt(), itemSearchRequest.getLastItemPrice(), sortBy, direction)
                )
                .orderBy(getSortCondition(sortBy, direction)) // 다중 정렬 적용
                .limit(size + 1)  // 다음 페이지 확인을 위해 +1 조회
                .fetch();

        // 다음 페이지 존재 여부 확인
        boolean hasNext = results.size() > size;
        CursorInfo nextCursorInfo = null;

        if (hasNext) {
            Item lastItem = results.remove(results.size() - 1);  // 마지막 데이터 제거하면서 반환

            // 정렬 기준에 따라 커서 정보를 생성
            if ("createdAt".equals(sortBy)) {
                nextCursorInfo = new CursorInfo(lastItem.getId(), lastItem.getCreatedAt(), null);
            } else if ("price".equals(sortBy)) {
                nextCursorInfo = new CursorInfo(lastItem.getId(), null, lastItem.getPrice());
            }
        }

        return new CursorPage<>(results.stream().map(ItemFindResponseDto::of).toList(), nextCursorInfo, hasNext);
    }

    // 아이템 이름 검색
    private BooleanExpression keywordContains(String keyword) {
        return StringUtils.hasText(keyword) ? QItem.item.itemName.containsIgnoreCase(keyword) : null;
    }


    private BooleanExpression cursorBasedPaging(Long cursorId, LocalDateTime cursorCreatedAt, Integer cursorItemPrice, String sortBy, String direction) {
        if (cursorId == null) { //cursorId가 null이면 첫페이지므로 null 반환.
            return null;
        }

        QItem item = QItem.item;
        boolean isDescending = "DESC".equalsIgnoreCase(direction);

        if ("createdAt".equals(sortBy)) {
            return isDescending
                    ? item.createdAt.lt(cursorCreatedAt)
                    .or(item.createdAt.eq(cursorCreatedAt).and(item.id.loe(cursorId)))
                    : item.createdAt.gt(cursorCreatedAt)
                    .or(item.createdAt.eq(cursorCreatedAt).and(item.id.goe(cursorId)));
        }

        if ("price".equals(sortBy)) {
            return isDescending
                    ? item.price.lt(cursorItemPrice)
                    .or(item.price.eq(cursorItemPrice).and(item.id.loe(cursorId)))
                    : item.price.gt(cursorItemPrice)
                    .or(item.price.eq(cursorItemPrice).and(item.id.goe(cursorId)));
        }

        return null;
    }

    ///  정렬 기준 설정
    private OrderSpecifier<?>[] getSortCondition(String sortBy, String direction) {
        QItem item = QItem.item;
        boolean isAscending = "ASC".equalsIgnoreCase(direction);

        // 기본 정렬 기준: ID 내림차순
        OrderSpecifier<?> primarySort;
        OrderSpecifier<?> secondarySort = isAscending ? item.id.asc() : item.id.desc();

        if ("createdAt".equals(sortBy)) {
            primarySort = isAscending ? item.createdAt.asc().nullsLast() : item.createdAt.desc().nullsFirst();
        } else if ("price".equals(sortBy)) {
            primarySort = isAscending ? item.price.asc().nullsLast() : item.price.desc().nullsFirst();
        } else {
            // 기본 정렬이 없을 경우, ID 기준 내림차순 정렬
            primarySort = item.id.desc();
        }

        return new OrderSpecifier[]{primarySort, secondarySort};
    }
}