package com.toanyone.user.user.domain.entity;

import com.toanyone.user.user.domain.UserRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private String nickName;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String slackId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    public static User createUser(String nickName, String password, String slackId, UserRole role) {
        User user = new User();
        user.nickName = nickName;
        user.password = password;
        user.slackId = slackId;
        user.role = role;
        return user;

    }
}
