package com.toanyone.delivery.domain.repository;

import com.toanyone.delivery.domain.DeliveryManager;

import java.util.List;
import java.util.Optional;

public interface DeliveryManagerRepository {
    DeliveryManager save(DeliveryManager deliveryManager);

    Optional<DeliveryManager> findById(Long id);

    Boolean existsByUserId(Long id);

    Optional<DeliveryManager> findByDeliveryManagerTypeAndId(DeliveryManager.DeliveryManagerType deliveryManagerType, Long id);

    List<DeliveryManager> findByDeliveryManagerTypeAndDeliveryOrderIn(
            DeliveryManager.DeliveryManagerType deliveryManagerType,
            List<Long> deliveryOrders
    );

    Optional<DeliveryManager> findByHubIdAndDeliveryOrder(Long hubId, Long deliveryOrder);

    Optional<DeliveryManager> findFirstByHubIdOrderByIdAsc(Long hubId);

    // 허브 배송 담당자 타입이면서 배송 순번이 주어진 수보다 작거나 같은 배송 담당자들을 조회
    List<DeliveryManager> findByDeliveryManagerTypeAndDeliveryOrderLessThanEqual(
            DeliveryManager.DeliveryManagerType deliveryManagerType, long maxDeliveryOrder);
}
