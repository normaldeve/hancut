package com.system.batch.killbatchsystem.user.security.filter;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.system.batch.killbatchsystem.user.security.application.provider.JwtProvider;
import com.system.batch.killbatchsystem.user.security.application.refresh.RefreshTokenService;
import com.system.batch.killbatchsystem.user.security.domain.LoginRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

  @Value("${security.jwt.access-token-validity-seconds}")
  private long accessTokenValiditySeconds;

  private final AuthenticationManager authenticationManager;

  private final JwtProvider jwtProvider;

  private final RefreshTokenService refreshTokenService;

  private final ObjectMapper objectMapper;

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
      HttpServletResponse response) throws AuthenticationException {

    if (!request.getMethod().equals("POST")) {
      log.warn("로그인 요청을 거부합니다 - 지원하지 않는 HTTP 메서드: {}", request.getMethod());
      throw new AuthenticationServiceException(
          "지원하지 않는 HTTP 메서드: " + request.getMethod());
    }

    try {
      LoginRequest loginRequest = objectMapper.readValue(
          request.getInputStream(), LoginRequest.class
      );

      String nickname = loginRequest.nickname();
      String password = loginRequest.password();

      log.debug("로그인 요청을 수신했습니다 - 요청자: {}, 경로: {}", nickname, request.getRequestURI());

      UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(nickname, password);

      return authenticationManager.authenticate(authToken);

    } catch (IOException e) {
      log.error("로그인 요청을 파싱하지 못했습니다 - 요청 본문을 해석하지 못했습니다", e);
      throw new AuthenticationServiceException("Failed to parse login request", e);
    }
  }

  @Override
  protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain, Authentication authResult) throws IOException, ServletException {

    String accessToken = jwtProvider.generateToken(authResult, accessTokenValiditySeconds);
    String refreshToken = refreshTokenService.registerRefreshToken(authResult);

    ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
        .httpOnly(true)
        .path("/")
        .sameSite("Lax")
        .maxAge(Duration.ofDays(7))
        // .secure(true)                 // 운영이 HTTPS면 켜기(개발 HTTP면 생략)
        .build();

    response.addHeader("Set-Cookie", refreshCookie.toString());

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.getWriter().write(
        objectMapper.writeValueAsString(accessToken)
    );

    log.debug("로그인에 성공했습니다");
  }

  @Override
  protected void unsuccessfulAuthentication(HttpServletRequest request,
      HttpServletResponse response, AuthenticationException failed)
      throws IOException, ServletException {
    log.debug("로그인에 실패했습니다");

    int status = HttpServletResponse.SC_UNAUTHORIZED;
    String code = "AUTHENTICATION_FAILED";
    String message = "인증에 실패했습니다.";

    if (failed instanceof UsernameNotFoundException) {
      code = "USER_NOT_FOUND";
      message = "존재하지 않는 닉네임입니다.";
    } else if (failed instanceof BadCredentialsException) {
      code = "BAD_CREDENTIALS";
      message = "비밀번호가 올바르지 않습니다.";
    } else if (failed instanceof LockedException) {
      code = "ACCOUNT_LOCKED";
      message = "계정이 잠겨 있습니다. 관리자에게 문의하세요.";
    }

    response.setStatus(status);
    response.setContentType("application/json; charset=UTF-8");
    response.setHeader("Cache-Control", "no-store");

    ObjectNode body = objectMapper.createObjectNode()
        .put("code", code)
        .put("message", message)
        .put("path", request.getRequestURI())
        .put("timestamp", java.time.OffsetDateTime.now().toString());

    response.getWriter().write(objectMapper.writeValueAsString(body));
  }
}
