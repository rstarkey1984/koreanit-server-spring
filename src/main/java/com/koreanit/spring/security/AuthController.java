package com.koreanit.spring.security;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.koreanit.spring.common.response.ApiResponse;
import com.koreanit.spring.user.UserMapper;
import com.koreanit.spring.user.UserService;
import com.koreanit.spring.user.dto.request.UserLoginRequest;
import com.koreanit.spring.user.dto.response.UserResponse;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
public class AuthController {

  public static final String SESSION_USER_ID = "LOGIN_USER_ID";

  private final UserService userService;

  public AuthController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/login")
  public ApiResponse<Long> login(@RequestBody UserLoginRequest req, HttpSession session) {
    Long userId = userService.login(req.getUsername(), req.getPassword());
    session.setAttribute(SESSION_USER_ID, userId);
    return ApiResponse.ok(userId);
  }

  @PostMapping("/logout")
  public ApiResponse<Void> logout(HttpSession session) {
    session.invalidate();
    return ApiResponse.ok();
  }

  // 정상 흐름: 현재 로그인 사용자 조회
  // (세션이 없거나 값이 없으면 다음 단계에서 401로 처리)
  @GetMapping("/me")
  public ApiResponse<UserResponse> me(HttpSession session) {
    Long userId = (Long) session.getAttribute(SESSION_USER_ID);
    return ApiResponse.ok(UserMapper.toResponse(userService.get(userId)));
  }

}