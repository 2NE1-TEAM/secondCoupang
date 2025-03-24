package com.toanyone.item.presentation.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자는 Spring이 필요로 해서 추가
public class ItemSearchRequest {
    private String keyword;  // 아이템 이름 검색
    private Long lastItemId;  // 커서 기반 페이징을 위한 마지막 ID
    private LocalDateTime lastCreatedAt; // 커서페이징을 위만 생성일
    private Integer lastItemPrice; // 커서페이징을 위한 마지막 아이템 가격
}