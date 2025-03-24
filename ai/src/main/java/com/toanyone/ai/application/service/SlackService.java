package com.toanyone.ai.application.service;

import com.toanyone.ai.common.exception.AIException;
import com.toanyone.ai.common.response.MultiResponse;
import com.toanyone.ai.common.response.SingleResponse;
import com.toanyone.ai.domain.entity.Ai;
import com.toanyone.ai.domain.entity.OrderStatus;
import com.toanyone.ai.domain.entity.SlackMessage;
import com.toanyone.ai.infrastructure.SlackRepository;
import com.toanyone.ai.presentation.dto.ResponseGetSlackDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;


@Service
@RequiredArgsConstructor
public class SlackService {

    private final WebClient slackWebClient;
    private final SlackRepository slackRepository;

    public void sendMessage(String message) {
        String payload = "{\"text\":\"" + message + "\"}";

        slackWebClient
                .post()
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(
                        response -> System.out.println("Message sent successfully"),
                        error -> System.err.println("Error sending message: " + error.getMessage())
                );
    }

    public void save(Ai ai, String message, OrderStatus success, HttpServletRequest request) {

        SlackMessage slackMessage = SlackMessage.createSlackMessage(ai, message, success);

        slackMessage.updateUpdated(Long.parseLong(request.getHeader("X-User-Id")));
        slackMessage.updateCreated(Long.parseLong(request.getHeader("X-User-Id")));
        slackRepository.save(slackMessage);
    }

    public ResponseEntity<MultiResponse<ResponseGetSlackDto>> getSlacks(Pageable pageable, String userRole) {
        isMaster(userRole);

        Page<ResponseGetSlackDto> dtoPage = this.slackRepository.findAllByDeletedAtIsNullOrderByIdDesc(pageable).map(ResponseGetSlackDto::new);

        return ResponseEntity.ok().body(MultiResponse.success(dtoPage));
    }

    public ResponseEntity<SingleResponse<ResponseGetSlackDto>> getSlack(Long id, String userRole) {
        isMaster(userRole);

        SlackMessage slackMessage = this.slackRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(AIException.NotFoundException::new);

         return ResponseEntity.ok().body(SingleResponse.success(new ResponseGetSlackDto(slackMessage)));
    }

    private void isMaster(String roles){
        if(!roles.equals("MASTER")){
            throw new AIException.UnAuthorized();
        }
    }

    public ResponseEntity<SingleResponse> deleteSlack(Long slackId, String role, Long userId) {
        isMaster(role);

        SlackMessage slack = this.slackRepository.findById(slackId).orElseThrow(AIException.NotFoundException::new);
        slack.updateDeleted(userId);
        slack.updateUpdated(userId);
        this.slackRepository.save(slack);

        return ResponseEntity.ok().body(SingleResponse.success("삭제 성공"));
    }
}
