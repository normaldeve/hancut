package com.system.batch.killbatchsystem.user.security.domain;

import com.system.batch.killbatchsystem.user.domain.User;
import java.time.Instant;
import lombok.Builder;

@Builder
public record JwtToken(
    Long id,
    User user,
    String token,
    Instant issueTime,
    Instant expirationTime
) {

}
