package com.system.batch.killbatchsystem.user.domain;

import jakarta.validation.constraints.NotBlank;

public record CreateUser(
    @NotBlank(message = "닉네임 입력은 필수입니다")
    String nickname,
    @NotBlank(message = "비밀번호 입력은 필수입니다")
    String password
) {

}
