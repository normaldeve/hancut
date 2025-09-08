package com.system.batch.killbatchsystem.comment.domain;

import jakarta.validation.constraints.NotBlank;

public record CreateComment(
    @NotBlank
    String author,
    @NotBlank
    String content
) {

}
