package com.system.batch.killbatchsystem.comment.infrastructure.jpa;

import io.micrometer.common.lang.Nullable;
import java.time.LocalDateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface CommentQueryRepository {
  Slice<CommentEntity> findSliceByAiSummaryId(
      Long aiSummaryId,
      @Nullable LocalDateTime cursorCreatedAt,
      @Nullable Long cursorId,
      Pageable pageable
  );
}
