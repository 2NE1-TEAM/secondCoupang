package com.toanyone.user.user.presentation;

import com.toanyone.user.user.application.service.UserService;
import com.toanyone.user.user.common.SingleResponse;
import com.toanyone.user.user.presentation.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
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

    @PostMapping("/sign-up-by-master")
    public ResponseEntity<SingleResponse<ResponseUserDto>> signUpByMaster(@Valid @RequestBody RequestCreateUserDto requestCreateUserDto,
                                                                          HttpServletRequest request) {

        ResponseUserDto responseUserDto = this.userService.signUpByMaster(requestCreateUserDto, request);

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


        return ResponseEntity.ok().body(SingleResponse.success("로그아웃 되었습니다. "));
    }

    @PatchMapping()
    public ResponseEntity<SingleResponse> editUser(@Valid @RequestBody RequestEditUserDto requestEditUserDto,
                                                   HttpServletRequest request) {
        ResponseEditUserDto responseEditUserDto = userService.editUser(requestEditUserDto, request);
        return ResponseEntity.status(HttpStatus.OK).body(SingleResponse.success(responseEditUserDto));
    }

    @DeleteMapping()
    public ResponseEntity<SingleResponse> deleteUser(@RequestParam Long userId,
                                                     HttpServletRequest request) {

        this.userService.deleteUser(userId, request);

        return ResponseEntity.ok().body(SingleResponse.success(userId +" 삭제 완료 "));
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
