package com.toanyone.order.infrastructure;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.toanyone.order.common.CursorInfo;
import com.toanyone.order.common.CursorPage;
import com.toanyone.order.domain.entity.Order;
import com.toanyone.order.presentation.dto.SortType;
import com.toanyone.order.presentation.dto.request.OrderFindAllRequestDto;
import com.toanyone.order.presentation.dto.request.OrderSearchRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

import static com.toanyone.order.domain.entity.QOrder.order;
import static com.toanyone.order.domain.entity.QOrderItem.orderItem;

@Slf4j(topic = "OrderQueryDslImpl")
@RequiredArgsConstructor
public class OrderQueryDslRepositoryImpl implements OrderQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public CursorPage<Order> search(OrderSearchRequestDto requestDto) {

        log.info("search");
        log.info("getCursorId : {}", requestDto.getCursorId());

        List<Order> results = queryFactory
                .selectFrom(order)
                .leftJoin(order.items, orderItem).fetchJoin()
                .where(
                        isUserEqualTo(requestDto.getUserId()),
                        containsKeyword(requestDto.getKeyword()),
                        cursorId(requestDto.getCursorId()),
                        cursorIdAndTimestamp(requestDto.getCursorId(), requestDto.getTimestamp(), requestDto.getSortType())
                )
                .orderBy(createOrderSpecifier(requestDto.getSortType()))
                .limit(requestDto.getSize() + 1) //nextCursorInfo를 위해 size보다 하나 더 가져오기
                .fetch();

        boolean hasNext = results.size() > requestDto.getSize();

        Order lastOrder = hasNext ? results.remove(results.size() - 1) : null; //마지막 값은 nextCursorInfo

        CursorInfo nextCursorInfo = createNextCursorInfo(lastOrder, requestDto.getSortType());

        return new CursorPage<>(results, nextCursorInfo, hasNext);

    }

    @Override
    public CursorPage<Order> findAll(Long userId, OrderFindAllRequestDto requestDto) {

        log.info("search");

        List<Order> results = queryFactory
                .selectFrom(order)
                .leftJoin(order.items, orderItem).fetchJoin()
                .where(
                        isUserEqualTo(userId),
                        cursorId(requestDto.getCursorId()),
                        cursorIdAndTimestamp(requestDto.getCursorId(), requestDto.getTimestamp(), requestDto.getSortType())
                )
                .orderBy(createOrderSpecifier(requestDto.getSortType()))
                .limit(requestDto.getSize() + 1) //nextCursorInfo를 위해 size보다 하나 더 가져오기
                .fetch();

        boolean hasNext = results.size() > requestDto.getSize();

        Order lastOrder = hasNext ? results.remove(results.size() - 1) : null; //마지막 값은 nextCursorInfo

        CursorInfo nextCursorInfo = createNextCursorInfo(lastOrder, requestDto.getSortType());

        return new CursorPage<>(results, nextCursorInfo, hasNext);

    }

    private BooleanExpression isUserEqualTo(Long userId) {
        return userId != null ? order.userId.eq(userId) : null;
    }

    private BooleanExpression cursorId(Long cursorId) {
        return cursorId != null ? order.id.lt(cursorId) : null;
    }

    private BooleanExpression containsKeyword(String keyword) {
        return StringUtils.hasText(keyword) ? orderItem.itemName.containsIgnoreCase(keyword) : null;
    }

    private BooleanExpression cursorIdAndTimestamp(Long cursorId, LocalDateTime timestamp, SortType sortType) {
        if (cursorId == null || timestamp == null) {
            return null;
        } else {
            return switch (sortType) {
                case CREATED_AT_DESC ->
                        order.createdAt.lt(timestamp)
                                .or(order.createdAt.eq(timestamp)
                                        .and(order.id.lt(cursorId)));
                case UPDATED_AT_DESC ->
                        order.updatedAt.lt(timestamp)
                                .or(order.updatedAt.eq(timestamp)
                                        .and(order.id.lt(cursorId)));
            };
        }
    }

    private OrderSpecifier createOrderSpecifier(SortType sortType) {
        return switch (sortType) {
            case CREATED_AT_DESC -> order.createdAt.desc();
            case UPDATED_AT_DESC -> order.updatedAt.desc();
            default -> null;
        };
    }

    private CursorInfo createNextCursorInfo(Order lastOrder, SortType sortType) {

        if (lastOrder == null) {
            return null;
        }

        Long cursorId = lastOrder.getId();
        LocalDateTime timestamp = null;

        switch (sortType) {
            case CREATED_AT_DESC -> timestamp = lastOrder.getCreatedAt();
            case UPDATED_AT_DESC -> timestamp = lastOrder.getUpdatedAt();
            default -> {
                return null;
            }
        }

        return new CursorInfo(cursorId, timestamp);
    }

}
