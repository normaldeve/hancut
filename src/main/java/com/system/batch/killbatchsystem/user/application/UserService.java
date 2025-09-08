package com.system.batch.killbatchsystem.user.application;

import com.system.batch.killbatchsystem.summary.domain.UserSearchCond;
import com.system.batch.killbatchsystem.user.domain.CreateUser;
import com.system.batch.killbatchsystem.user.domain.User;
import com.system.batch.killbatchsystem.user.security.domain.UpdateUserRequest;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

  User createUser(CreateUser request);

  Page<User> search(UserSearchCond cond, Pageable pageable);

  User update(String nickname, UpdateUserRequest request, Optional<MultipartFile> profile);

  User findByNickname(String nickname);
}
