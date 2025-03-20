package com.toanyone.ai.domain.entity;


import com.toanyone.ai.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "p_slack_message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SlackMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "slack_message_id")
    private Long id;
    private Long userId;
    private String message;
    private LocalDateTime shippingTime;

    @OneToOne(fetch = FetchType.LAZY)
    private Ai ai;

}
