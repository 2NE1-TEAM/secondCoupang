package com.toanyone.user.user.application.service;


import com.toanyone.user.user.domain.dto.RequestCreateUserDto;
import com.toanyone.user.user.domain.dto.RequestLoginUserDto;
import com.toanyone.user.user.domain.dto.ResponseUserDto;
import com.toanyone.user.user.domain.entity.User;
import com.toanyone.user.user.domain.UserRepository;
import com.toanyone.user.user.infrastructure.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final String HEADER_STRING = "Authorization";


    public ResponseUserDto signUp(RequestCreateUserDto requestCreateUserDto) {

         userRepository.findUserBySlackId(requestCreateUserDto.getSlackId()).ifPresent(user ->
         { throw new RuntimeException("존재하는 slack Id 입니다. "); });

        User user = User.createUser(requestCreateUserDto.getNickName(), encryptPassword(requestCreateUserDto.getPassword()), requestCreateUserDto.getSlackId(), requestCreateUserDto.getRole());
        userRepository.save(user);

        return new ResponseUserDto(user.getId(), user.getNickName(), user.getPassword(), user.getSlackId(), user.getRole());
    }

    private String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }


    public void signIn(RequestLoginUserDto requestLoginUserDto, HttpServletResponse response) {

        User user = this.userRepository.findUserBySlackId(requestLoginUserDto.getSlackId()).orElseThrow(() ->
                new NoSuchElementException("존재하지 않는 아이디 입니다.")
        );

        if(!passwordEncoder.matches(requestLoginUserDto.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다. ");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        response.setHeader(HEADER_STRING, token);

    }


}
