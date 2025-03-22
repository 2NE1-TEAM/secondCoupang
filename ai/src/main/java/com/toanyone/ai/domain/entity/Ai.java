package com.toanyone.ai.domain.entity;

import com.toanyone.ai.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_ai")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ai extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ai_id")
    private Long id;

    @Column(nullable = false, length = 4000)
    private String question;
    @Column(nullable = false)
    private String answer;

    public static Ai createAi(String question, String answer){
        Ai ai = new Ai();
        ai.question = question;
        ai.answer = answer;

        return ai;
    }
}
