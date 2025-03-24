package com.toanyone.item.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor

/**
 * 정렬 조건이 생성순, 가격 두개라는 가정.
 * 지원하는 정렬에 따라 이 클래스에 필드는 추가됨.
 */
public class CursorInfo implements Serializable {
    private Long lastItemId;
    private LocalDateTime lastCreatedAt;
    private Integer lastItemPrice;

}
