package com.toanyone.store.infrastructure.configuration;

import com.toanyone.store.domain.exception.StoreException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

@Component
public class HubClientErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 400) {
            return new StoreException.HubNotFoundException("존재하지 않는 허브입니다.");
        } else if (response.status() >= 500) {
            return new StoreException.HubServerErrorException("허브 서버에 문제가 발생했습니다.");
        }
        return defaultDecoder.decode(methodKey, response);
    }
}
