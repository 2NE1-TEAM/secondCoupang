package com.toanyone.item.infrastructure.configuration;

import com.toanyone.item.domain.exception.ItemException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

@Component
public class StoreClientErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 400) {
            return new ItemException.StoreNotFoundException("존재하지 않는 업체입니다.");
        } else if (response.status() >= 500) {
            return new ItemException.StoreServerErrorException("스토어 서버에 문제가 발생했습니다.");
        }
        return defaultDecoder.decode(methodKey, response);
    }
}
