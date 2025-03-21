package com.toanyone.hub.application.service;

import com.toanyone.hub.presentation.dto.RouteDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;


@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoDirectionService {

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    private final RestTemplate restTemplate;

    public RouteDTO getDirections(double originLng, double originLat, double destinationLng, double destinationLat) {
        log.info("카카오 거리 찾기 API 호출 전 :: originLng = {}, originLat = {}, destinationLng = {}, destinationLat = {}", originLng, originLat, destinationLng, destinationLat);
        String url = String.format(
                "https://apis-navi.kakaomobility.com/v1/directions?origin=%f,%f&destination=%f,%f",
                originLng, originLat, destinationLng, destinationLat
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        log.info("카카오 Request URL: " + url);  // 요청 URL 로그 출력

        try {
            ResponseEntity<RouteDTO> response = restTemplate.exchange(url, HttpMethod.GET, entity, RouteDTO.class);
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.warn("카카오 API 호출 실패 - 상태 코드: {}, 응답 바디: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException();
        } catch (Exception e) {
            log.error("카카오 API 호출 중 알 수 없는 예외 발생", e);
            throw new RuntimeException();
        }
    }
}