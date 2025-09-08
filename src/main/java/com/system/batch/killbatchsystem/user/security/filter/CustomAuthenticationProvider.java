package com.system.batch.killbatchsystem.user.security.filter;

import com.system.batch.killbatchsystem.user.security.infrastructure.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

  private final UserDetailsService userDetailsService;
  private final PasswordEncoder passwordEncoder;

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    if (!supports(authentication.getClass())) {
      return null;
    }

    String username = authentication.getName();
    String password = authentication.getCredentials().toString();

    AuthUser user;
    try {
      user = (AuthUser) userDetailsService.loadUserByUsername(username);
    } catch (UsernameNotFoundException e) {
      throw new UsernameNotFoundException("존재하지 않는 아이디입니다.");
    }

    if (user.getUser().isLocked()) {
      throw new LockedException("계정이 잠겨 있습니다. 관리자에게 문의하세요");
    }

    if (password == null || !passwordEncoder.matches(password, user.getPassword())) {
      throw new BadCredentialsException("비밀번호가 올바르지 않습니다.");
    }

    UsernamePasswordAuthenticationToken result =
        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    result.setDetails(authentication.getDetails());
    return result;
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
