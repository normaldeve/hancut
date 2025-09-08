package com.system.batch.killbatchsystem.user.security.application.provider;

import com.system.batch.killbatchsystem.user.domain.Role;
import com.system.batch.killbatchsystem.user.domain.User;
import com.system.batch.killbatchsystem.user.security.domain.JwtToken;
import com.system.batch.killbatchsystem.user.security.infrastructure.AuthUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtProviderImpl implements JwtProvider {

  @Value("${security.jwt.secret}")
  private String secret;

  @Override
  public String generateToken(Authentication authentication, long tokenValidityInSeconds) {
    Instant issueTime = Instant.now();
    Instant expirationTime = issueTime.plus(Duration.ofSeconds(tokenValidityInSeconds));

    Key key = getKey();
    AuthUser authUser = (AuthUser) authentication.getPrincipal();
    User user = authUser.getUser();

    return Jwts.builder()
        .setSubject(authentication.getName())
        .claim("role", authUser.getUser().role())
        .claim("locked", authUser.getUser().isLocked())
        .setIssuedAt(Date.from(issueTime))
        .setExpiration(Date.from(expirationTime))
        .signWith(key, SignatureAlgorithm.HS512)
        .compact();
  }

  @Override
  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token);
      return true;
    } catch (SecurityException | MalformedJwtException e) {
      log.info("Invalid Jwt Token : {}", e.getMessage());
    } catch (ExpiredJwtException e) {
      log.info("Expired Jwt Token : {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
      log.info("Unsupported Jwt Token : {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      log.info("Jwt Token is empty : {}", e.getMessage());
    }
    return false;
  }

  @Override
  public Authentication getAuthenticationFromToken(String token) {
    Claims claims = Jwts.parserBuilder().setSigningKey(getKey()).build()
        .parseClaimsJws(token).getBody();

    String role = claims.get("role", String.class);
    var authorities = role == null
        ? List.<GrantedAuthority>of()
        : List.of(new SimpleGrantedAuthority("ROLE_" + role));

    User user = User.builder()
        .nickname(claims.getSubject())
        .role(role != null ? Role.valueOf(role) : null)
        .isLocked(Boolean.TRUE.equals(claims.get("locked", Boolean.class)))
        .build();

    return new UsernamePasswordAuthenticationToken(new AuthUser(user), null, authorities);
  }

  @Override
  public JwtToken parseToken(String token) {
    try {
      Claims body = Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token)
          .getBody();

      String nickname = body.getSubject();
      Date iat = body.get("iat", Date.class);
      Date exp = body.get("exp", Date.class);
      Boolean isLocked = body.get("locked", Boolean.class);
      String roleStr = body.get("role", String.class);
      Role role = roleStr != null ? Role.valueOf(roleStr) : null;

      User user = User.builder()
          .nickname(nickname)
          .role(role)
          .isLocked(isLocked)
          .build();

      return JwtToken.builder()
          .user(user)
          .token(token)
          .issueTime(iat.toInstant())
          .expirationTime(exp.toInstant())
          .build();
    } catch (JwtException e) {
      log.error("Error parsing Jwt Token: {}", e.getMessage());
      throw new RuntimeException("Error parsing Jwt Token");
    }
  }

  private Key getKey() {
    byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
