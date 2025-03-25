package com.toanyone.order.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CursorInfo {
    private Long nextCursorOrderId;
    private LocalDateTime timestamp;
}