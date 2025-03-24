package com.toanyone.ai.infrastructure;

import com.toanyone.ai.domain.entity.SlackMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;


public interface SlackRepository extends JpaRepository<SlackMessage, Long> {

    Page<SlackMessage> findAllByDeletedAtIsNullOrderByIdDesc(Pageable pageable);

    Optional<SlackMessage> findByIdAndDeletedAtIsNull(Long id);


}
