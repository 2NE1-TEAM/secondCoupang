package com.toanyone.user.user.domain;

import com.toanyone.user.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserBySlackId(String slackId);

    Optional<User> findUserByIdAndDeletedAtIsNull(Long id);
}
