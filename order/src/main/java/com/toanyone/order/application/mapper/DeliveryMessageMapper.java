package com.toanyone.order.application.mapper;

import com.toanyone.order.application.dto.message.OrderDeliveryMessage;
import com.toanyone.order.application.dto.request.OrderCreateServiceDto;
import org.springframework.stereotype.Component;

@Component
public class DeliveryMessageMapper {

    public OrderDeliveryMessage toOrderDeliveryMessage(OrderCreateServiceDto orderCreateRequest) {

        return OrderDeliveryMessage.builder()
                .receiveStoreId(orderCreateRequest.getReceiveStoreId())
                .supplyStoreId(orderCreateRequest.getSupplyStoreId())
                .recipient(orderCreateRequest.getDeliveryInfo().getRecipient())
                .build();

    }

}
