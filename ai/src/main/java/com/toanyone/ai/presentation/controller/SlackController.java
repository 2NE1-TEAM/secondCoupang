package com.toanyone.ai.presentation.controller;

import com.toanyone.ai.application.service.SlackService;
import com.toanyone.ai.presentation.dto.ResponseGetSlackDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/slack")
@RequiredArgsConstructor
public class SlackController {

    private final SlackService slackService;

    @GetMapping
    public ResponseEntity<Page<ResponseGetSlackDto>> getSlacks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request)
    {
        Pageable pageable = PageRequest.of(page, size);
        return slackService.getSlacks(pageable, request.getHeader("X-User-Roles"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseGetSlackDto> getIdSlacks(
            @PathVariable Long id,
            HttpServletRequest request)
    {

        return slackService.getSlack(id, request.getHeader("X-User-Roles"));
    }
}
