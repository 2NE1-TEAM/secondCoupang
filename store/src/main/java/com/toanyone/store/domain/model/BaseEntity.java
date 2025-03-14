package com.toanyone.store.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Getter
public abstract class BaseEntity {
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt; // 생성날짜

    @LastModifiedDate
    private LocalDateTime updatedAt; // 수정날짜

    @CreatedBy
    @Column(updatable = false)
    private Long createdBy; // 생성자ID

    @LastModifiedBy
    private Long updatedBy; // 수정자ID

    private LocalDateTime deletedAt; // 삭제날짜
    private Long deletedBy; // 삭제자ID

    /**
     * 엔티티 삭제 처리 로직 구현 해야 함.
     */

}
