package com.system.batch.killbatchsystem.user.security.filter;

import com.system.batch.killbatchsystem.user.domain.User;
import com.system.batch.killbatchsystem.user.security.application.provider.JwtProvider;
import com.system.batch.killbatchsystem.user.security.domain.SecurityMatchers;
import com.system.batch.killbatchsystem.user.security.infrastructure.AuthUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtProvider jwtProvider;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain chain
  ) throws ServletException, IOException {

    final String uri = request.getRequestURI();
    log.info("JWT 필터 진입 - [{}] {}", request.getMethod(), uri);

    if (SecurityMatchers.isPublic(request)) {
      log.info("공개 경로: 인증 스킵 - {}", uri);
      chain.doFilter(request, response);
      return;
    }

    Optional<String> optToken = resolveAccessToken(request);

    if (optToken.isEmpty()) {
      log.info("Authorization 헤더 없음 - {}", uri);
      chain.doFilter(request, response);
      return;
    }

    String token = optToken.get();

    if (!jwtProvider.validateToken(token)) {
      log.warn("액세스 토큰 무효 - {}", uri);
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      return;
    }

    User user = jwtProvider.parseToken(token).user();
    AuthUser principal = new AuthUser(user);

    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    log.info("인증 성공 - user={}, authorities={}", principal.getUsername(), principal.getAuthorities());

    chain.doFilter(request, response);
  }

  private Optional<String> resolveAccessToken(HttpServletRequest request) {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header == null) return Optional.empty();
    String v = header.trim();
    if (v.length() >= 7 && v.substring(0, 7).toLowerCase(Locale.ROOT).equals("bearer ")) {
      String token = v.substring(7).trim();
      return token.isEmpty() ? Optional.empty() : Optional.of(token);
    }
    return Optional.empty();
  }
}