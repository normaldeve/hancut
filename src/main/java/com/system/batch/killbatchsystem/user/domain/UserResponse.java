package com.system.batch.killbatchsystem.user.domain;

import java.time.LocalDateTime;

public record UserResponse(
    Long id,
    String nickname,
    String profileImageUrl,
    Boolean isLocked,
    Role role,
    LocalDateTime createdAt
) {

  public static UserResponse from(User u) {
    return new UserResponse(
        u.id(),
        u.nickname(),
        u.profileImageUrl(),
        u.isLocked(),
        u.role(),
        u.createdAt()
    );
  }

}
