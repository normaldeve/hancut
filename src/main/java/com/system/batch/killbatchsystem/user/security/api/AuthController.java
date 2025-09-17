package com.system.batch.killbatchsystem.user.security.api;

import com.system.batch.killbatchsystem.user.application.UserService;
import com.system.batch.killbatchsystem.user.application.valid.NicknameValidService;
import com.system.batch.killbatchsystem.user.domain.User;
import com.system.batch.killbatchsystem.user.domain.UserResponse;
import com.system.batch.killbatchsystem.user.domain.ValidNickname;
import com.system.batch.killbatchsystem.user.security.application.refresh.RefreshTokenService;
import com.system.batch.killbatchsystem.user.security.domain.JwtToken;
import com.system.batch.killbatchsystem.user.security.infrastructure.AuthUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

  private final NicknameValidService nicknameValidService;

  private final RefreshTokenService refreshTokenService;

  private final UserService userService;

  @PostMapping("refresh")
  public ResponseEntity<String> refresh(
      @CookieValue("refresh_token") String refreshToken
  ) {
    log.info("엑세스 토큰 재발급을 요청합니다");
    String access = refreshTokenService.refreshAccessToken(refreshToken);

    return ResponseEntity.ok(access);
  }

  @GetMapping("/me")
  public ResponseEntity<UserResponse> me(
      @AuthenticationPrincipal AuthUser authUser
  ) {

    User user = userService.findByNickname(authUser.getUser().nickname());

    return ResponseEntity.ok(UserResponse.from(user));
  }

  @PostMapping("/validate-nickname")
  public ResponseEntity<Boolean> validateNickname(@RequestBody ValidNickname request) {
    boolean isValid = nicknameValidService.validateNickname(request.nickname());

    return ResponseEntity.ok(isValid);
  }

  @PostMapping("/exist-nickname")
  public ResponseEntity<Boolean> existNickname(@RequestBody ValidNickname request) {
    boolean isExist = nicknameValidService.existsNickname(request.nickname());

    return ResponseEntity.ok(isExist);
  }
}
