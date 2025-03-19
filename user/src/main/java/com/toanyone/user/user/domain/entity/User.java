package com.toanyone.user.user.domain.entity;

import com.toanyone.user.user.common.BaseEntity;
import com.toanyone.user.user.domain.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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

    @Column(nullable = false)
    private Long hubId;

    public static User createUser(String nickName, String password, String slackId, UserRole role, Long hubId) {
        User user = new User();
        user.nickName = nickName;
        user.password = password;
        user.slackId = slackId;
        user.role = role;
        user.hubId = hubId;
        return user;

    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateRole(@NotNull UserRole role) {
        this.role = role;
    }

    public void updateNickName(@NotNull String nickName) {
        this.nickName = nickName;
    }
}
