package com.system.batch.killbatchsystem.user.domain;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder(toBuilder = true)
public record User(
    Long id,
    String nickname,
    String encodedPass,
    String profileImageUrl,
    Boolean isLocked,
    Role role,
    LocalDateTime createdAt
) {

  public static User createUser(CreateUser request) {
    return User.builder()
        .nickname(request.nickname())
        .isLocked(false)
        .role(Role.USER)
        .build();
  }

  public static User createAdmin(String nickname, String encodedPass) {
    return User.builder()
        .nickname(nickname)
        .encodedPass(encodedPass)
        .isLocked(false)
        .role(Role.ADMIN)
        .build();
  }

  public User encodePassword(String encodedPass) {
    return this.toBuilder()
        .encodedPass(encodedPass)
        .build();
  }
}
