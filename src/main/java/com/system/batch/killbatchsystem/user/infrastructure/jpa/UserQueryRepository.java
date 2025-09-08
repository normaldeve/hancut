package com.system.batch.killbatchsystem.user.infrastructure.jpa;

import com.system.batch.killbatchsystem.summary.domain.UserSearchCond;
import com.system.batch.killbatchsystem.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserQueryRepository {

  Page<User> search(UserSearchCond cond, Pageable pageable);

}
