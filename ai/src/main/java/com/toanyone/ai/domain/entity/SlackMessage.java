package com.toanyone.ai.domain.entity;


import com.toanyone.ai.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_slack_message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SlackMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "slack_message_id")
    private Long id;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(nullable = false)
    private OrderStatus orderStatus;

    @OneToOne(fetch = FetchType.LAZY)
    private Ai ai;


    public static SlackMessage createSlackMessage(Ai ai, String message, OrderStatus orderStatus) {
        SlackMessage slackMessage = new SlackMessage();
        slackMessage.ai = ai;
        slackMessage.message = message;
        slackMessage.orderStatus = orderStatus;

        return slackMessage;
    }
}
