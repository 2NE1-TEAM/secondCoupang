package com.toanyone.delivery.infrastructure.db;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.toanyone.delivery.application.dto.response.GetDeliveryManagerResponseDto;
import com.toanyone.delivery.common.utils.MultiResponse.CursorInfo;
import com.toanyone.delivery.common.utils.MultiResponse.CursorPage;
import com.toanyone.delivery.domain.DeliveryManager;
import com.toanyone.delivery.domain.DeliveryManager.DeliveryManagerType;
import com.toanyone.delivery.domain.repository.CustomDeliveryMangerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.toanyone.delivery.domain.QDeliveryManager.deliveryManager;

@Repository
@RequiredArgsConstructor
public class CustomDeliverManagerRepositoryImpl implements CustomDeliveryMangerRepository {
    private final JPAQueryFactory queryFactory;


    BooleanExpression searchByDeliveryManagerType(DeliveryManagerType deliveryManagerType) {
        return deliveryManagerType != null ? deliveryManager.deliveryManagerType.eq(deliveryManagerType) : null;
    }

    BooleanExpression searchByDeliveryManagerId(Long deliveryManagerId) {
        return deliveryManagerId != null ? deliveryManager.id.gt(deliveryManagerId) : null;
    }

    BooleanExpression searchByUserId(Long userId) {
        return userId != null ? deliveryManager.userId.eq(userId) : null;
    }

    BooleanExpression searchByName(String name) {
        return name != null ? deliveryManager.name.contains(name) : null;
    }

    OrderSpecifier<LocalDateTime> sortBy(String sortBy) {
        if (sortBy == null) {
            return deliveryManager.createdAt.asc();
        }
        if ((!sortBy.equals("오름차순")) && (!sortBy.equals("내림차순"))) {
            return deliveryManager.createdAt.asc();

        }
        return sortBy.equals("오름차순") ? deliveryManager.createdAt.asc() : deliveryManager.createdAt.desc();
    }

    @Override
    public CursorPage<GetDeliveryManagerResponseDto> getDeliveryManagers(Long deliveryManagerId, String sortBy, DeliveryManagerType deliveryManagerType, Long userId, String name, int limit) {
        List<DeliveryManager> deliveryManagers = queryFactory.selectFrom(deliveryManager)
                .from(deliveryManager)
                .where(searchByDeliveryManagerType(deliveryManagerType), searchByDeliveryManagerId(deliveryManagerId)
                        , searchByUserId(userId), searchByName(name))
                .limit(limit + 1)
                .orderBy(sortBy(sortBy))
                .fetch();

        boolean hasNext = deliveryManagers.size() > limit;

        if (hasNext) {
            deliveryManagers.remove(deliveryManagers.size() - 1);
        }

        List<GetDeliveryManagerResponseDto> responseDtos = deliveryManagers.stream()
                .map(GetDeliveryManagerResponseDto::from)
                .toList();

        if (responseDtos.isEmpty()) {
            return new CursorPage<>(null, null, false);
        }

        long cursorId = responseDtos.stream()
                .mapToLong(GetDeliveryManagerResponseDto::getDeliveryManagerId)
                .max().getAsLong();

        return new CursorPage<>(responseDtos,
                new CursorInfo(cursorId),
                hasNext);
    }

    @Override
    public Long nextDeliveryOrder(Long hubId) {
        final Long initialDeliverOrder = 1L;

        Long nextDeliveryOrderNumber = queryFactory
                .select(deliveryManager.deliveryOrder.max().add(1))
                .from(deliveryManager)
                .where(deliveryManager.hubId.eq(hubId))
                .fetchFirst();

        if (nextDeliveryOrderNumber != null) {
            return nextDeliveryOrderNumber;
        }
        return initialDeliverOrder;
    }
}
