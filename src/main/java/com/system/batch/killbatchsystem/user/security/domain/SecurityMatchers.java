package com.system.batch.killbatchsystem.user.security.domain;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

public final class SecurityMatchers {

  private SecurityMatchers() {}

  // 공개(permitAll)로 두려는 엔드포인트 상수
  public static final String SSE = "/api/notifications/stream";
  public static final String VALIDATE_NICKNAME = "/auth/validate-nickname";
  public static final String EXISTS_NICKNAME = "/auth/exist-nickname";
  public static final String SIGN_IN = "/api/users";
  public static final String LOGIN   = "/auth/login";
  public static final String LOGOUT  = "/auth/logout";
  public static final String REFRESH = "/auth/refresh";
  public static final String CSRF    = "/api/auth/csrf-token";
  public static final String SUMMARY = "/summary/**";

  // 공개 엔드포인트 매처 (메서드까지 명시)
  private static final List<RequestMatcher> PUBLIC_MATCHERS = List.of(
      new AntPathRequestMatcher(SSE,   HttpMethod.GET.name()),
      new AntPathRequestMatcher(VALIDATE_NICKNAME,   HttpMethod.POST.name()),
      new AntPathRequestMatcher(EXISTS_NICKNAME,   HttpMethod.POST.name()),
      new AntPathRequestMatcher(SIGN_IN,   HttpMethod.POST.name()),
      new AntPathRequestMatcher(LOGIN,   HttpMethod.POST.name()),
      new AntPathRequestMatcher(LOGOUT,  HttpMethod.POST.name()),
      new AntPathRequestMatcher(REFRESH, HttpMethod.POST.name()),
      new AntPathRequestMatcher(CSRF,    HttpMethod.GET.name()),
      new AntPathRequestMatcher(SUMMARY, HttpMethod.GET.name())
  );

  public static boolean isPublic(HttpServletRequest request) {
    for (RequestMatcher m : PUBLIC_MATCHERS) {
      if (m.matches(request)) return true;
    }
    return false;
  }
}
