package com.toanyone.user.user.application.service;


import com.toanyone.user.user.common.exception.UserException;
import com.toanyone.user.user.domain.UserRole;
import com.toanyone.user.user.presentation.dto.RequestCreateUserDto;
import com.toanyone.user.user.presentation.dto.RequestDeleteUserDto;
import com.toanyone.user.user.presentation.dto.RequestLoginUserDto;
import com.toanyone.user.user.presentation.dto.ResponseUserDto;
import com.toanyone.user.user.domain.entity.User;
import com.toanyone.user.user.domain.UserRepository;
import com.toanyone.user.user.infrastructure.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
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
         { throw new UserException.AlreadyExistedSlackId(); });

        User user = User.createUser(requestCreateUserDto.getNickName(), encryptPassword(requestCreateUserDto.getPassword()), requestCreateUserDto.getSlackId(), requestCreateUserDto.getRole(), requestCreateUserDto.getHubId());
        userRepository.save(user);

        return new ResponseUserDto(user.getId(), user.getNickName(), user.getPassword(), user.getSlackId(), user.getRole(), user.getHubId());
    }

    private String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }


    public void signIn(RequestLoginUserDto requestLoginUserDto, HttpServletResponse response) {

        User user = this.userRepository.findUserBySlackId(requestLoginUserDto.getSlackId()).orElseThrow(() ->
                new UserException.NoExistSlackId()
        );

        if(!passwordEncoder.matches(requestLoginUserDto.getPassword(), user.getPassword())) {
            throw new UserException.NotCorrectPassword();
        }

        String token = jwtUtil.generateAccessToken(user.getId(), user.getRole(), user.getSlackId(), user.getHubId(), user.getNickName());
        response.setHeader(HEADER_STRING, token);

        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        redisTemplate.opsForValue().set(refresh+":"+user.getId(), refreshToken, 7, TimeUnit.DAYS);
    }


    public void deleteUser(Long userId, HttpServletRequest request) {

        String roles = request.getHeader("X-User-Roles");
        if(!UserRole.MASTER.toString().equals(roles)){
            throw new UserException.NotAuthorize();
        }

        User user = this.userRepository.findUserByIdAndDeletedAtIsNull(userId).orElseThrow(()->new UserException.NoExistId());

        Long masterId = Long.parseLong( request.getHeader("X-User-Id"));
        user.updateDeleted(masterId);
        userRepository.save(user);
    }
}
