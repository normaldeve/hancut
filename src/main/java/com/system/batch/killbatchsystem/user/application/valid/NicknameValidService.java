package com.system.batch.killbatchsystem.user.application.valid;

public interface NicknameValidService {

  boolean validateNickname(String nickname);

  boolean existsNickname(String nickname);
}
