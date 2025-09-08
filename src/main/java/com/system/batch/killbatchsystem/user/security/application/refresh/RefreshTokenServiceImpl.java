package com.system.batch.killbatchsystem.user.security.application.refresh;

import com.system.batch.killbatchsystem.user.application.UserRepository;
import com.system.batch.killbatchsystem.user.domain.User;
import com.system.batch.killbatchsystem.user.security.application.JwtTokenRepository;
import com.system.batch.killbatchsystem.user.security.application.provider.JwtProvider;
import com.system.batch.killbatchsystem.user.security.domain.JwtToken;
import com.system.batch.killbatchsystem.user.security.infrastructure.AuthUser;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

  @Value("${security.jwt.access-token-validity-seconds}")
  private long accessTokenValiditySeconds;

  @Value("${security.jwt.refresh-token-validity-seconds}")
  private long refreshTokenValiditySeconds;

  private final JwtProvider jwtProvider;

  private final JwtTokenRepository jwtTokenRepository;

  private final UserRepository userRepository;

  @Override
  @Transactional
  public String registerRefreshToken(Authentication authentication) {
    String refreshToken = jwtProvider.generateToken(authentication, refreshTokenValiditySeconds);

    AuthUser userDetails = (AuthUser) authentication.getPrincipal();

    JwtToken jwtToken = JwtToken.builder()
        .token(refreshToken)
        .user(userDetails.getUser())
        .issueTime(Instant.now())
        .expirationTime(Instant.now().plusSeconds(refreshTokenValiditySeconds))
        .build();

    jwtTokenRepository.save(jwtToken);

    return refreshToken;
  }

  @Override
  public String refreshAccessToken(String refreshToken) {
    if (!jwtProvider.validateToken(refreshToken)) {
      throw new RuntimeException("Invalid refresh token");
    }

    JwtToken token = jwtTokenRepository.findByRefreshToken(refreshToken)
        .orElseThrow(() -> new RuntimeException("Can not found refresh token in DB"));

    JwtToken jwtToken = jwtProvider.parseToken(refreshToken);
    String nickname = jwtToken.user().nickname();

    User user = userRepository.findByNickname(nickname)
        .orElseThrow(() -> new RuntimeException("DB에서 해당하는 사용자를 찾을 수 없습니다 -> 닉네임 : " + nickname));

    if (Boolean.TRUE.equals(user.isLocked())) {
      throw new RuntimeException("사용자 계정이 잠겨있어 토큰 재발급을 취소합니다 -> 닉네임 : " + nickname);
    }

    var authorities = user.role() == null
        ? List.<GrantedAuthority>of()
        : List.of(new SimpleGrantedAuthority("ROLE_" + user.role().name()));

    Authentication auth =
        new UsernamePasswordAuthenticationToken(new AuthUser(user), null, authorities);

    return jwtProvider.generateToken(auth, accessTokenValiditySeconds);
  }

  @Override
  @Transactional
  public void deleteRefreshToken(String refreshToken) {
    log.info("DB에 저장된 Refresh 토큰을 제거합니다");

    JwtToken jwtToken = jwtProvider.parseToken(refreshToken);

    User user = jwtToken.user();

    log.info("Refresh 토큰을 받았습니다: {}", user.nickname());

    jwtTokenRepository.deleteByNickname(user.nickname());

  }
}
