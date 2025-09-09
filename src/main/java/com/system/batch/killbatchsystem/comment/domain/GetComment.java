package com.system.batch.killbatchsystem.comment.domain;

import java.util.List;
import org.springframework.data.domain.Page;

public record GetComment(
    List<Comments> content,
    int page,
    int totalPages,
    boolean last,
    int size,
    int numberOfElements,
    long totalElements
) {
  public static GetComment of(Page<Comments> page) {
    return new GetComment(
        page.getContent(),
        page.getNumber(),
        page.getTotalPages(),
        page.isLast(),
        page.getSize(),
        page.getNumberOfElements(),
        page.getTotalElements()
    );
  }
}
