package com.toanyone.ai.common;


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

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(updatable = false)
    protected LocalDateTime createdAt= LocalDateTime.now();

    @CreatedBy
    @Column
    protected Long createdBy;

    @LastModifiedDate
    protected LocalDateTime updatedAt;

    @LastModifiedBy
    protected Long updatedBy;

    protected LocalDateTime deletedAt;
    protected Long deletedBy;

    public void updateDeleted(Long userId) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = userId;
    }

    public void updateCreated(Long userId) {
        this.createdAt = LocalDateTime.now();
        this.createdBy = userId;
    }

    public void updateUpdated(Long userId) {
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = userId;
    }

}
