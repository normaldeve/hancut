package com.system.batch.killbatchsystem.user.application.valid;

import com.system.batch.killbatchsystem.user.application.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NicknameValidServiceImpl implements NicknameValidService {

  private final UserRepository userRepository;

  /**
   * TODO S3에 slang.csv 업로드 후 다시 작성할 것
   * @param nickname
   * @return
   */
  @Override
  @Transactional(readOnly = true)
  public boolean validateNickname(String nickname) {

    return true;
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsNickname(String nickname) {
    return userRepository.existsByNickname(nickname);
  }
}
