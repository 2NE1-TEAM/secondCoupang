package com.toanyone.ai.infrastructure;

import com.toanyone.ai.domain.entity.Ai;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiRepository extends JpaRepository<Ai, Long> {
}
