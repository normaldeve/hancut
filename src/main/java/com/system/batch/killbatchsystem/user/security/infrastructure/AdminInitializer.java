package com.system.batch.killbatchsystem.user.security.infrastructure;

import com.system.batch.killbatchsystem.user.security.application.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AdminInitializer implements ApplicationRunner {

  private final AuthService authService;

  @Override
  public void run(ApplicationArguments args) {
    authService.initAdmin();
  }
}
