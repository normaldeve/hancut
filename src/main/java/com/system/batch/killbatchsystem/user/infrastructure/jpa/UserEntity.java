package com.system.batch.killbatchsystem.user.infrastructure.jpa;

import com.system.batch.killbatchsystem.user.domain.Role;
import com.system.batch.killbatchsystem.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 255, unique = true)
  private String nickname;

  @Column(name = "encoded_pass", nullable = false, length = 255)
  private String encodedPass;

  @Column(name = "profile_image_url", length = 500)
  private String profileImageUrl;

  @Column(name = "is_locked", nullable = false)
  private Boolean isLocked = false;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private Role role;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Builder
  public UserEntity(String nickname, String encodedPass, String profileImageUrl, Boolean isLocked, Role role) {
    this.nickname = nickname;
    this.encodedPass = encodedPass;
    this.profileImageUrl = profileImageUrl;
    this.isLocked = isLocked;
    this.role = role;
  }

  public void update(String nickname) {
    this.nickname = nickname;
  }

  public void update(String nickname, String imageUrl) {
    this.nickname = nickname;
    this.profileImageUrl = imageUrl;
  }

  public static User toModel(UserEntity entity) {
    return User.builder()
        .id(entity.id)
        .nickname(entity.nickname)
        .encodedPass(entity.encodedPass)
        .profileImageUrl(entity.profileImageUrl)
        .isLocked(entity.isLocked)
        .role(entity.role)
        .createdAt(entity.createdAt)
        .build();
  }

  public static UserEntity fromModel(User user) {
    return UserEntity.builder()
        .nickname(user.nickname())
        .encodedPass(user.encodedPass())
        .profileImageUrl(user.profileImageUrl())
        .isLocked(user.isLocked())
        .role(user.role())
        .build();
  }
}
