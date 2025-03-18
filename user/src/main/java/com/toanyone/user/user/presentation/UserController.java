package com.toanyone.user.user.presentation;

import com.toanyone.user.user.application.service.UserService;
import com.toanyone.user.user.common.SingleResponse;
import com.toanyone.user.user.domain.dto.RequestCreateUserDto;
import com.toanyone.user.user.domain.dto.RequestLoginUserDto;
import com.toanyone.user.user.domain.dto.ResponseUserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping("/sign-up")
    public ResponseEntity<SingleResponse<ResponseUserDto>> signUp(@Valid @RequestBody RequestCreateUserDto requestCreateUserDto) {

//        log.info("requestCreateUserDto:{}", requestCreateUserDto);
        ResponseUserDto responseUserDto = this.userService.signUp(requestCreateUserDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(SingleResponse.success(responseUserDto));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<SingleResponse<String>> signIn(@Valid @RequestBody RequestLoginUserDto requestLoginUserDto,
                                    HttpServletResponse response) {

        this.userService.signIn(requestLoginUserDto, response);

        return ResponseEntity.ok().body(SingleResponse.success(requestLoginUserDto.getSlackId()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {


        return ResponseEntity.ok().body(SingleResponse.success(null));
    }

    @GetMapping("/test")
    public String test(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        String userRole = request.getHeader("X-User-Roles");
        String slackId = request.getHeader("X-Slack-Id");
        String hubId = request.getHeader("X-Hub-Id");
        String nickName = request.getHeader("X-Nick-Name");
        return "test :" + userId + "," + userRole + "," + slackId + "," + hubId + "," + nickName;
    }
}
