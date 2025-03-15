package com.toanyone.order.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
public class OrderCreateRequestDto {

    @NotNull
    private Long userId;

    @NotNull
    private Long supplyStoreId;

    @NotNull
    private Long receiveStoreId;

    @NotNull
    private List<ItemRequestDto> items;

    @NotNull
    private DeliveryRequestDto deliveryInfo;

    @Getter
    @AllArgsConstructor
    public static class ItemRequestDto {

        @NotNull
        private Long itemId;

        @NotNull
        private int quantity;

    }

    @Getter
    @AllArgsConstructor
    public static class DeliveryRequestDto {

        @NotBlank
        private String deliveryAddress;

        @NotBlank
        private String recipient;

    }


}

