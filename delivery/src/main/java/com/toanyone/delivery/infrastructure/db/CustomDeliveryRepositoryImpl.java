package com.toanyone.delivery.infrastructure.db;

import static com.toanyone.delivery.domain.QDelivery.delivery;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.toanyone.delivery.common.utils.MultiResponse;
import com.toanyone.delivery.common.utils.MultiResponse.CursorPage;
import com.toanyone.delivery.domain.Delivery;
import com.toanyone.delivery.domain.Delivery.DeliveryStatus;
import com.toanyone.delivery.domain.repository.CustomDeliveryRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CustomDeliveryRepositoryImpl implements CustomDeliveryRepository {

  private final JPAQueryFactory queryFactory;


  @Override
  public CursorPage<Delivery> getDeliveriesWithCursor(Long deliveryId,
      Delivery.DeliveryStatus deliveryStatus, Long departureHubId,
      Long arrivalHubId, String recipient, Long storeDeliveryManagerId, int limit, String sortBy) {

    List<Delivery> deliveries = queryFactory.selectFrom(delivery)
        .from(delivery)
        .where(searchByDeliveryId(deliveryId),
            searchByArrivalHubId(arrivalHubId),
            searchByDepartureHubId(departureHubId),
            searchByRecipient(recipient),
            searchByStoreDeliveryManagerId(storeDeliveryManagerId),
            searchByDeliveryStatus(deliveryStatus))
//        .orderBy(sortBy(sortBy))
        .limit(limit + 1)
        .fetch();

    boolean hasNext = deliveries.size() > limit;

    if (hasNext) {
      deliveries.remove(deliveries.size() - 1);
    }

//        List<GetDeliveryResponseDto> responseDtos = deliveries.stream()
//                .map(GetDeliveryResponseDto::from)
//                .toList();

//        long cursorId = responseDtos.stream()
//                .mapToLong(GetDeliveryResponseDto::getDeliveryId)
//                .max().getAsLong();
    long cursorId = deliveries.stream()
        .mapToLong(Delivery::getId)
        .max().getAsLong();

    return new CursorPage<>(deliveries,
        new MultiResponse.CursorInfo(cursorId),
        hasNext);

  }

  @Override
  public Page<Delivery> getDeliveriesWithOffset(Pageable pageable, Long deliveryId,
      DeliveryStatus deliveryStatus, Long departureHubId, Long arrivalHubId, String recipient,
      Long storeDeliveryManagerId, int limit, String sortBy) {
    List<Delivery> deliveries = queryFactory.selectFrom(delivery)
        .from(delivery)
        .where(
            searchByArrivalHubId(arrivalHubId),
            searchByDepartureHubId(departureHubId),
            searchByRecipient(recipient),
            searchByStoreDeliveryManagerId(storeDeliveryManagerId),
            searchByDeliveryStatus(deliveryStatus))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
//        .orderBy(sortBy(sortBy))
        .fetch();

    return new PageImpl<>(deliveries, pageable, deliveries.size());
  }

  OrderSpecifier<LocalDateTime> sortBy(String sortBy) {
    if (sortBy == null) {
      return delivery.createdAt.asc();
    }
    if ((!sortBy.equals("오름차순")) && (!sortBy.equals("내림차순"))) {
      return delivery.createdAt.asc();

    }
    return sortBy.equals("오름차순") ? delivery.createdAt.asc() : delivery.createdAt.desc();
  }

  BooleanExpression searchByDeliveryId(Long deliveryId) {
    return deliveryId != null ? delivery.id.gt(deliveryId) : null;
  }

  BooleanExpression searchByDeliveryStatus(Delivery.DeliveryStatus deliveryStatus) {
    return deliveryStatus != null ? delivery.deliveryStatus.eq(deliveryStatus) : null;
  }

  BooleanExpression searchByArrivalHubId(Long arrivalHubId) {
    return arrivalHubId != null ? delivery.arrivalHubId.eq(arrivalHubId) : null;
  }

  BooleanExpression searchByDepartureHubId(Long departureHubId) {
    return departureHubId != null ? delivery.departureHubId.eq(departureHubId) : null;
  }

  BooleanExpression searchByStoreDeliveryManagerId(Long storeDeliveryManagerId) {
    return storeDeliveryManagerId != null ? delivery.storeDeliveryManagerId.eq(
        storeDeliveryManagerId) : null;
  }

  BooleanExpression searchByRecipient(String recipient) {
    return recipient != null ? delivery.recipient.eq(recipient) : null;
  }


}


