package com.toanyone.store.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreSearchRequest {
    private String keyword;  // 가게명 검색
    private String telephone; // 가게 전화번호
    private Long hubId;  // 허브 ID 필터링
    private Long lastStoreId;  // 커서 기반 페이징을 위한 마지막 ID
    private LocalDateTime lastCreatedAt; // 커서페이징을 위만 생성일
    private String lastStoreName; // 커서페이징을 위한 가게이름
}