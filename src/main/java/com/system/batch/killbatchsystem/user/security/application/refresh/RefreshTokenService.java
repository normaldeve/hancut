package com.system.batch.killbatchsystem.user.security.application.refresh;

import com.system.batch.killbatchsystem.user.security.domain.JwtToken;
import org.springframework.security.core.Authentication;

public interface RefreshTokenService {

  String registerRefreshToken(Authentication authentication);

  String refreshAccessToken(String refreshToken);

  void deleteRefreshToken(String accessToken);
}
