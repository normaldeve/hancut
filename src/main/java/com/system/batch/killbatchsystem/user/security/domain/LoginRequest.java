package com.system.batch.killbatchsystem.user.security.domain;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "닉네임 입력은 필수입니다")
    String nickname,
    @NotBlank(message = "비밀번호 입력은 필수입니다")
    String password
) {

}
