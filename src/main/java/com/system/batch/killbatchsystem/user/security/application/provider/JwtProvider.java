package com.system.batch.killbatchsystem.user.security.application.provider;

import com.system.batch.killbatchsystem.user.security.domain.JwtToken;
import org.springframework.security.core.Authentication;

/**
 * JwtProvider는 토큰을 생성하고 검증하고 토큰에서 정보를 꺼낸다
 */
public interface JwtProvider {

  String generateToken(Authentication authentication, long tokenValidityInSeconds);

  boolean validateToken(String token);

  Authentication getAuthenticationFromToken(String token);

  JwtToken parseToken(String token);
}
