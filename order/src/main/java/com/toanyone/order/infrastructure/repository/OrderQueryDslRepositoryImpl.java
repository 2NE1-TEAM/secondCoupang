package com.toanyone.order.infrastructure.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.toanyone.order.application.dto.request.OrderFindAllCondition;
import com.toanyone.order.application.dto.request.OrderSearchCondition;
import com.toanyone.order.common.dto.CursorInfo;
import com.toanyone.order.common.dto.CursorPage;
import com.toanyone.order.domain.model.Order;
import com.toanyone.order.common.dto.SortType;
import com.toanyone.order.infrastructure.repository.OrderQueryDslRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

import static com.toanyone.order.domain.model.QOrder.order;
import static com.toanyone.order.domain.model.QOrderItem.orderItem;

@Slf4j(topic = "OrderQueryDslImpl")
@RequiredArgsConstructor
public class OrderQueryDslRepositoryImpl implements OrderQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public CursorPage<Order> search(OrderSearchCondition request) {

        log.info("search");
        log.info("getCursorId : {}", request.getCursorId());

        List<Long> orderIds = queryFactory
                .select(orderItem.order.id)
                .from(orderItem)
                .where(containsKeyword(request.getKeyword()))
                .fetch();

        List<Order> results = queryFactory
                .selectFrom(order)
                .where(
                        isUserEqualTo(request.getUserId()),
                        order.id.in(orderIds),
                        cursorId(request.getCursorId()),
                        cursorIdAndTimestamp(request.getCursorId(), request.getTimestamp(), request.getSortType())
                )
                .orderBy(createOrderSpecifier(request.getSortType()))
                .limit(request.getSize() + 1)
                .fetch();

        boolean hasNext = results.size() > request.getSize();

        Order lastOrder = hasNext ? results.remove(results.size() - 1) : null;

        results.forEach(order -> Hibernate.initialize(order.getItems()));

        CursorInfo nextCursorInfo = createNextCursorInfo(lastOrder, request.getSortType());

        return new CursorPage<>(results, nextCursorInfo, hasNext);

    }

    @Override
    public CursorPage<Order> findAll(Long userId, OrderFindAllCondition request) {

        log.info("search");

        List<Order> results = queryFactory
                .selectFrom(order)
                .where(
                        isUserEqualTo(userId),
                        cursorId(request.getCursorId()),
                        cursorIdAndTimestamp(request.getCursorId(), request.getTimestamp(), request.getSortType())
                )
                .orderBy(createOrderSpecifier(request.getSortType()))
                .limit(request.getSize() + 1)
                .fetch();

        boolean hasNext = results.size() > request.getSize();

        Order lastOrder = hasNext ? results.remove(results.size() - 1) : null;

        results.forEach(order -> Hibernate.initialize(order.getItems()));

        CursorInfo nextCursorInfo = createNextCursorInfo(lastOrder, request.getSortType());

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
