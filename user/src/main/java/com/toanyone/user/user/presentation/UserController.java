package com.toanyone.user.user.presentation;

import com.toanyone.user.user.application.service.UserService;
import com.toanyone.user.user.domain.dto.RequestCreateUserDto;
import com.toanyone.user.user.domain.dto.ResponseUserDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping("/sign-up")
    public ResponseEntity<ResponseUserDto> signUp(@Valid @RequestBody RequestCreateUserDto requestCreateUserDto) {

        log.info("requestCreateUserDto:{}", requestCreateUserDto);
        ResponseUserDto responseUserDto = this.userService.signUp(requestCreateUserDto);

        return ResponseEntity.ok(responseUserDto);
    }
}
