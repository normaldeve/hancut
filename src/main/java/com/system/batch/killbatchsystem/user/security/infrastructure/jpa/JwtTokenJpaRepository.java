package com.system.batch.killbatchsystem.user.security.infrastructure.jpa;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JwtTokenJpaRepository extends JpaRepository<JwtTokenEntity, Long> {

  void deleteByNickname(String nickname);

  Optional<JwtTokenEntity> findByRefreshToken(String refreshToken);
}
