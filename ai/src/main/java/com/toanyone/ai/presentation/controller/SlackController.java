package com.toanyone.ai.presentation.controller;

import com.toanyone.ai.application.service.SlackService;
import com.toanyone.ai.common.response.MultiResponse;
import com.toanyone.ai.common.response.SingleResponse;
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
    public ResponseEntity<MultiResponse<ResponseGetSlackDto>> getSlacks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request)
    {
        Pageable pageable = PageRequest.of(page, size);
        return slackService.getSlacks(pageable, request.getHeader("X-User-Roles"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SingleResponse<ResponseGetSlackDto>> getIdSlacks(
            @PathVariable Long id,
            HttpServletRequest request)
    {

        return slackService.getSlack(id, request.getHeader("X-User-Roles"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SingleResponse> deleteSlack(@PathVariable Long id,
                                                      HttpServletRequest request){

        return slackService.deleteSlack(id, request.getHeader("X-User-Roles"),
                Long.parseLong(request.getHeader("X-User-Id")));
    }
}
