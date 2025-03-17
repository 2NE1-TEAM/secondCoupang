package com.toanyone.user.user.presentation;

import com.toanyone.user.user.application.service.UserService;
import com.toanyone.user.user.domain.dto.RequestCreateUserDto;
import com.toanyone.user.user.domain.dto.RequestLoginUserDto;
import com.toanyone.user.user.domain.dto.ResponseUserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@Valid @RequestBody RequestLoginUserDto requestLoginUserDto,
                                    HttpServletResponse response) {

        this.userService.signIn(requestLoginUserDto, response);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/test")
    public String test(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        String userRole = request.getHeader("X-User-Roles");
        String slackId = request.getHeader("X-Slack-Id");
        return "test :" + userId + "," + userRole + "," + slackId;
    }
}
