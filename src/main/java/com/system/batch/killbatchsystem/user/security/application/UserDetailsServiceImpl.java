package com.system.batch.killbatchsystem.user.security.application;

import com.system.batch.killbatchsystem.user.application.UserRepository;
import com.system.batch.killbatchsystem.user.domain.User;
import com.system.batch.killbatchsystem.user.security.infrastructure.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String nickname) throws UsernameNotFoundException {
    User user = userRepository.findByNickname(nickname)
        .orElseThrow(() -> new UsernameNotFoundException(nickname));

    return new AuthUser(user);
  }
}
