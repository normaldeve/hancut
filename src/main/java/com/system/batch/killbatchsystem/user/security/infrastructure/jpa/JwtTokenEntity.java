package com.system.batch.killbatchsystem.user.security.infrastructure.jpa;

import com.system.batch.killbatchsystem.user.security.domain.JwtToken;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_token")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JwtTokenEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String nickname;

  @Column(columnDefinition = "varchar(512)", nullable = false, unique = true)
  private String refreshToken;

  @Column(columnDefinition = "timestamp with time zone", nullable = false)
  private Instant issuedAt;

  @Column(columnDefinition = "timestamp with time zone", nullable = false)
  private Instant expirationTime;

  @Builder
  public JwtTokenEntity(String nickname, String refreshToken, Instant issuedAt, Instant expirationTime) {
    this.nickname = nickname;
    this.refreshToken = refreshToken;
    this.issuedAt = issuedAt;
    this.expirationTime = expirationTime;
  }

  public static JwtToken toModel(JwtTokenEntity jwtTokenEntity) {
    return JwtToken.builder()
        .id(jwtTokenEntity.id)
        .token(jwtTokenEntity.refreshToken)
        .issueTime(jwtTokenEntity.issuedAt)
        .expirationTime(jwtTokenEntity.expirationTime)
        .build();
  }

  public static JwtTokenEntity fromModel(JwtToken jwtToken) {
    return JwtTokenEntity.builder()
        .nickname(jwtToken.user().nickname())
        .refreshToken(jwtToken.token())
        .issuedAt(jwtToken.issueTime())
        .expirationTime(jwtToken.expirationTime())
        .build();
  }
}
