package com.system.batch.killbatchsystem.user.security.application;

import com.system.batch.killbatchsystem.user.security.domain.JwtToken;
import java.util.Optional;

public interface JwtTokenRepository {

  void save(JwtToken jwtToken);

  void deleteByNickname(String nickname);

  Optional<JwtToken> findByRefreshToken(String refreshToken);

}
