package com.toanyone.user.user.application.service;


import com.toanyone.user.user.common.exception.UserException;
import com.toanyone.user.user.domain.dto.RequestCreateUserDto;
import com.toanyone.user.user.domain.dto.RequestLoginUserDto;
import com.toanyone.user.user.domain.dto.ResponseUserDto;
import com.toanyone.user.user.domain.entity.User;
import com.toanyone.user.user.domain.UserRepository;
import com.toanyone.user.user.infrastructure.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserService {

    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final String HEADER_STRING = "Authorization";
    @Value("${service.jwt.refresh}")
    private String refresh;


    public ResponseUserDto signUp(RequestCreateUserDto requestCreateUserDto) {

         userRepository.findUserBySlackId(requestCreateUserDto.getSlackId()).ifPresent(user ->
         { throw new UserException.NoExistId(); });

        User user = User.createUser(requestCreateUserDto.getNickName(), encryptPassword(requestCreateUserDto.getPassword()), requestCreateUserDto.getSlackId(), requestCreateUserDto.getRole(), requestCreateUserDto.getHubId());
        userRepository.save(user);

        return new ResponseUserDto(user.getId(), user.getNickName(), user.getPassword(), user.getSlackId(), user.getRole(), user.getHubId());
    }

    private String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }


    public void signIn(RequestLoginUserDto requestLoginUserDto, HttpServletResponse response) {

        User user = this.userRepository.findUserBySlackId(requestLoginUserDto.getSlackId()).orElseThrow(() ->
                new UserException.NoExistId()
        );

        if(!passwordEncoder.matches(requestLoginUserDto.getPassword(), user.getPassword())) {
            throw new UserException.NotCorrectPassword();
        }

        String token = jwtUtil.generateAccessToken(user.getId(), user.getRole(), user.getSlackId(), user.getHubId(), user.getNickName());
        response.setHeader(HEADER_STRING, token);

        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        redisTemplate.opsForValue().set(refresh+":"+user.getId(), refreshToken, 7, TimeUnit.DAYS);
    }


}
