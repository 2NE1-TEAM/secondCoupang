package com.toanyone.order.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
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
    @NoArgsConstructor
    public static class ItemRequestDto {

        @NotNull
        private Long itemId;

        @NotNull
        private String itemName;

        @NotNull
        private int price;

        @NotNull
        private int quantity;

    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DeliveryRequestDto {

        @NotBlank
        private String deliveryAddress;

        @NotBlank
        private String recipient;

    }


}

