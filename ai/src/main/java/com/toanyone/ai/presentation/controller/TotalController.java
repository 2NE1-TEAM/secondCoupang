package com.toanyone.ai.presentation.controller;

import com.toanyone.ai.application.service.TotalService;
import com.toanyone.ai.presentation.dto.RequestCreateMessageDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/total")
@RequiredArgsConstructor
public class TotalController {

    private final TotalService totalService;

    @PostMapping()
    public void sendMessageToAiAndSlack(@RequestBody RequestCreateMessageDto requestCreateMessageDto, HttpServletRequest request) {

        totalService.sendMessageToAiAndSlack(requestCreateMessageDto, request);
    }
}
