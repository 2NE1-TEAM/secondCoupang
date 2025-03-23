package com.toanyone.hub.infrastructure.configuration;

import com.toanyone.hub.domain.exception.HubException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

@Component
public class SlackClientErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() >= 400 && response.status() <= 499) {
            return new HubException.SlackServerErrorException("요청 데이터가 올바르지 않습니다.");
        } else if (response.status() >= 500) {
            return new HubException.SlackServerErrorException("슬랙 서버에 문제가 발생했습니다.");
        }
        return defaultDecoder.decode(methodKey, response);
    }
}
