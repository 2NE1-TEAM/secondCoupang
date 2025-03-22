package com.toanyone.ai.infrastructure;

import com.toanyone.ai.domain.entity.SlackMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface SlackRepository extends JpaRepository<SlackMessage, Long> {

    Page<SlackMessage> findAllByOrderByIdDesc(Pageable pageable);
}
