package com.system.batch.killbatchsystem.user.security.filter;

import com.system.batch.killbatchsystem.user.security.application.refresh.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

@Slf4j
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

  private final RefreshTokenService refreshTokenService;

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) {

    log.info("로그아웃 요청을 수신했습니다");
    resolveRefreshToken(request)
        .ifPresent(refreshToken -> {
          refreshTokenService.deleteRefreshToken(refreshToken);
          invalidateRefreshTokenCookie(response);
        });
  }

  private Optional<String> resolveRefreshToken(HttpServletRequest request) {
    return Arrays.stream(request.getCookies())
        .filter(cookie -> cookie.getName().equals("refresh_token"))
        .findFirst()
        .map(Cookie::getValue);
  }

  private void invalidateRefreshTokenCookie(HttpServletResponse response) {
    ResponseCookie del = ResponseCookie.from("refresh_token", "")
        .httpOnly(true)
        .path("/")
        .maxAge(0)
        .sameSite("None")
        .secure(true)
        .build();
    response.addHeader("Set-Cookie", del.toString());
  }
}
