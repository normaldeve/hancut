package com.system.batch.killbatchsystem.summary.domain;

import com.system.batch.killbatchsystem.user.domain.Role;

public record UserSearchCond(
    String nickname,
    Role role
) {

}
