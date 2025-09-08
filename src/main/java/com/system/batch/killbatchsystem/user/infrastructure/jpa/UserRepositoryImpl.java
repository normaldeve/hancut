package com.system.batch.killbatchsystem.user.infrastructure.jpa;

import com.system.batch.killbatchsystem.summary.domain.UserSearchCond;
import com.system.batch.killbatchsystem.user.application.UserRepository;
import com.system.batch.killbatchsystem.user.domain.User;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

  private final UserJpaRepository userJpaRepository;

  @Override
  public boolean existsByNickname(String nickname) {
    return userJpaRepository.existsByNickname(nickname);
  }

  @Override
  public User save(User user) {
    UserEntity userEntity = userJpaRepository.save(UserEntity.fromModel(user));

    return UserEntity.toModel(userEntity);
  }

  @Override
  public Optional<User> findByNickname(String nickname) {
    return userJpaRepository.findByNickname(nickname)
        .map(UserEntity::toModel);
  }

  @Override
  public Page<User> search(UserSearchCond cond, Pageable pageable) {
    return userJpaRepository.search(cond, pageable);
  }

  @Override
  public User update(String nickname, String newNickname, String imageUrl) {
    UserEntity userEntity = userJpaRepository.findByNickname(nickname)
        .orElseThrow(() -> new RuntimeException("User not found"));

    userEntity.update(newNickname, imageUrl);

    return UserEntity.toModel(userEntity);
  }

  @Override
  public User update(String nickname, String newNickname) {
    UserEntity userEntity = userJpaRepository.findByNickname(nickname)
        .orElseThrow(() -> new RuntimeException("User not found"));

    userEntity.update(newNickname);

    return UserEntity.toModel(userEntity);
  }
}
