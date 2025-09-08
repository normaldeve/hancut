package com.system.batch.killbatchsystem.user.application;

import com.system.batch.killbatchsystem.summary.domain.UserSearchCond;
import com.system.batch.killbatchsystem.user.domain.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepository {

  boolean existsByNickname(String nickname);

  User save(User user);

  Optional<User> findByNickname(String nickname);

  Page<User> search(UserSearchCond cond, Pageable pageable);

  User update(String nickname, String newNickname, String imageUrl);

  User update(String nickname, String newNickname);

}
