package com.toanyone.user.user.application.service;


import com.toanyone.user.user.common.exception.UserException;
import com.toanyone.user.user.domain.UserRole;
import com.toanyone.user.user.presentation.dto.*;
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

        User user = User.createUser(requestCreateUserDto.getNickName(), encryptPassword(requestCreateUserDto.getPassword()), requestCreateUserDto.getSlackId(), requestCreateUserDto.getRole(), requestCreateUserDto.getHubId(), requestCreateUserDto.getPhone());
        userRepository.save(user);

        return new ResponseUserDto(user.getId(), user.getNickName(), user.getPassword(), user.getSlackId(), user.getRole(), user.getHubId(), user.getPhone());
    }

    public ResponseUserDto signUpByMaster(@Valid RequestCreateUserDto requestCreateUserDto, HttpServletRequest request) {
        roleIsMaster(request);

        userRepository.findUserBySlackId(requestCreateUserDto.getSlackId()).ifPresent(user ->
        { throw new UserException.AlreadyExistedSlackId(); });

        User user = User.createUser(requestCreateUserDto.getNickName(), encryptPassword(requestCreateUserDto.getPassword()), requestCreateUserDto.getSlackId(), requestCreateUserDto.getRole(), requestCreateUserDto.getHubId(), requestCreateUserDto.getPhone());
        Long masterId = Long.parseLong(request.getHeader("X-User-Id"));

        user.updateCreated(masterId);

        userRepository.save(user);

        return new ResponseUserDto(user.getId(), user.getNickName(), user.getPassword(), user.getSlackId(), user.getRole(), user.getHubId(), user.getPhone());
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

        String token = jwtUtil.generateAccessToken(user.getId(), user.getRole(), user.getSlackId(), user.getHubId(), user.getNickName(), user.getPhone());
        response.setHeader(HEADER_STRING, token);

        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        redisTemplate.opsForValue().set(refresh+":"+user.getId(), refreshToken, 7, TimeUnit.DAYS);
    }


    public void deleteUser(Long userId, HttpServletRequest request) {

        roleIsMaster(request);

        User user = this.userRepository.findUserByIdAndDeletedAtIsNull(userId).orElseThrow(()->new UserException.NoExistId());

        Long masterId = Long.parseLong( request.getHeader("X-User-Id"));
        user.updateDeleted(masterId);
        userRepository.save(user);
    }


    private void roleIsMaster(HttpServletRequest request) {
        String roles = request.getHeader("X-User-Roles");
        if(!UserRole.MASTER.toString().equals(roles)){
            throw new UserException.NotAuthorize();
        }
    }

    public ResponseEditUserDto editUser(@Valid RequestEditUserDto requestEditUserDto,
                                        HttpServletRequest request) {
        roleIsMaster(request);
        User user = this.userRepository.findById(requestEditUserDto.getUserId()).orElseThrow(() -> new UserException.NoExistId());

        if(requestEditUserDto.getNewPassword() !=null && requestEditUserDto.getPassword() != null){
            if(!passwordEncoder.matches(requestEditUserDto.getPassword(), user.getPassword())){
                throw new UserException.NotCorrectPassword();
            }else{
                user.updatePassword(encryptPassword(requestEditUserDto.getNewPassword()));
            }
        }

        user.updateRole(requestEditUserDto.getRole());
        user.updateNickName(requestEditUserDto.getNickName());
        user.updateUpdated(Long.valueOf((request.getHeader("X-User-Id"))));
        user.updatePhone(requestEditUserDto.getPhone());
        this.userRepository.save(user);

        return ResponseEditUserDto.createResponseEditUserDto(user);

    }
}
