package com.toanyone.delivery.infrastructure;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.toanyone.delivery.application.dtos.response.GetDeliveryManagerResponseDto;
import com.toanyone.delivery.domain.DeliveryManager;
import com.toanyone.delivery.domain.DeliveryManager.DeliveryManagerType;
import com.toanyone.delivery.domain.repository.CustomDeliveryMangerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

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

    @Override
    public Page<GetDeliveryManagerResponseDto> getDeliveryManagers(Pageable pageable, Long deliveryManagerId, DeliveryManagerType deliveryManagerType) {
        List<DeliveryManager> deliveryManagers = queryFactory.selectFrom(deliveryManager)
                .from(deliveryManager)
                .where(searchByDeliveryManagerId(deliveryManagerId), searchByDeliveryManagerType(deliveryManagerType))
                .limit(pageable.getPageSize())
                .fetch();

        List<GetDeliveryManagerResponseDto> responseDtos = deliveryManagers.stream()
                .map(GetDeliveryManagerResponseDto::from)
                .toList();

        long total = queryFactory.selectFrom(deliveryManager)
                .where(searchByDeliveryManagerId(deliveryManagerId), searchByDeliveryManagerType(deliveryManagerType))
                .fetch()
                .size();


        return new PageImpl<>(responseDtos, pageable, total);
    }

}
