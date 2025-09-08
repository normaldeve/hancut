package com.system.batch.killbatchsystem.user.application;

import com.system.batch.killbatchsystem.summary.domain.UserSearchCond;
import com.system.batch.killbatchsystem.user.domain.CreateUser;
import com.system.batch.killbatchsystem.user.domain.User;
import com.system.batch.killbatchsystem.user.infrastructure.ImageUploader;
import com.system.batch.killbatchsystem.user.security.domain.UpdateUserRequest;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final ImageUploader imageUploader;

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public User createUser(CreateUser request) {
    User u = User.createUser(request);

    String encodedPass = passwordEncoder.encode(request.password());

    User user = u.encodePassword(encodedPass);

    return userRepository.save(user);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @Override
  @Transactional(readOnly = true)
  public Page<User> search(UserSearchCond cond, Pageable pageable) {
    return userRepository.search(cond, pageable);
  }

  @Override
  @Transactional
  public User update(String nickname, UpdateUserRequest request, Optional<MultipartFile> profile) {
    User user = userRepository.findByNickname(nickname)
        .orElseThrow(() -> new IllegalArgumentException("닉네임에 해당하는 사용자를 찾을 수 없습니다"));

    if (profile.isPresent() && !profile.get().isEmpty()) {
      MultipartFile file = profile.get();

      if (user.profileImageUrl() != null && !user.profileImageUrl().isBlank()) {
        imageUploader.deleteImage(user.profileImageUrl());
      }

      String imageURL = imageUploader.upload(file);

      return userRepository.update(nickname, request.newNickname(), imageURL);
    }
    return userRepository.update(nickname, request.newNickname());
  }

  @Override
  public User findByNickname(String nickname) {
    return userRepository.findByNickname(nickname)
        .orElseThrow(() -> new RuntimeException("닉네임에 해당하는 사용자를 찾을 수 없습니다: " + nickname));
  }
}
