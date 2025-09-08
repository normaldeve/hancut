package com.system.batch.killbatchsystem.user.security.application;

import com.system.batch.killbatchsystem.user.application.UserRepository;
import com.system.batch.killbatchsystem.user.domain.CreateUser;
import com.system.batch.killbatchsystem.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

  @Value("${hancut.admin.nickname}")
  private String nickname;
  @Value("${hancut.admin.password}")
  private String password;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void initAdmin() {

    if (userRepository.existsByNickname(nickname)) {
      log.info("어드민 계정이 이미 존재합니다");
      return;
    }

    String encodedPassword = passwordEncoder.encode(password);
    User admin = User.createAdmin(nickname, encodedPassword);

    log.info("어드민 계정이 초기화 되었습니다");
    userRepository.save(admin);
  }
}
