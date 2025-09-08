package com.system.batch.killbatchsystem.user.infrastructure.jpa;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long>, UserQueryRepository {

  boolean existsByNickname(String nickname);

  Optional<UserEntity> findByNickname(String nickname);
}
