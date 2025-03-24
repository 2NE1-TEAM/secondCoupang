package com.toanyone.store.domain.model;

import com.toanyone.store.common.filter.UserContext;
import com.toanyone.store.domain.exception.StoreException;
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
     * 엔티티 삭제할 경우 호출하기
     */
    public void delete() {
        if (this.deletedAt != null) throw new StoreException.StoreAlreadyDeletedException("이미 삭제된 엔티티입니다.");
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = UserContext.getUser().getUserId();;
    }
}
