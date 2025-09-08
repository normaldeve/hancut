package com.system.batch.killbatchsystem.user.security.infrastructure;

import com.system.batch.killbatchsystem.user.security.application.JwtTokenRepository;
import com.system.batch.killbatchsystem.user.security.domain.JwtToken;
import com.system.batch.killbatchsystem.user.security.infrastructure.jpa.JwtTokenEntity;
import com.system.batch.killbatchsystem.user.security.infrastructure.jpa.JwtTokenJpaRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JwtTokenRepositoryImpl implements JwtTokenRepository {

  private final JwtTokenJpaRepository jwtTokenJpaRepository;

  @Override
  public void save(JwtToken jwtToken) {
    JwtTokenEntity jwtTokenEntity = JwtTokenEntity.fromModel(jwtToken);

    jwtTokenJpaRepository.save(jwtTokenEntity);
  }

  @Override
  public void deleteByNickname(String nickname) {
    jwtTokenJpaRepository.deleteByNickname(nickname);
  }

  @Override
  public Optional<JwtToken> findByRefreshToken(String refreshToken) {
    return jwtTokenJpaRepository.findByRefreshToken(refreshToken)
        .map(JwtTokenEntity::toModel);
  }
}
