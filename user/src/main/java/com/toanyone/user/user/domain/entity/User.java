package com.toanyone.user.user.domain.entity;

import com.toanyone.user.user.domain.UserRole;
import jakarta.persistence.*;

@Entity
@Table(name = "p_user")
public class User extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickName;

    private String password;

    private String slackId;

    private UserRole role;
}
