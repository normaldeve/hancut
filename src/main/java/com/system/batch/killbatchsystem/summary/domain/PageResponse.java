package com.system.batch.killbatchsystem.summary.domain;

import java.util.List;
import org.springframework.data.domain.Page;


public record PageResponse<T>(
    List<T> cards,
    int page,
    int totalPages,
    boolean last,
    int size,
    int numberOfElements,
    long totalElements
) {
  public static <T> PageResponse<T> of(Page<T> page) {
    return new PageResponse<>(
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
