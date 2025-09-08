package com.system.batch.killbatchsystem.comment.application;

import com.system.batch.killbatchsystem.comment.domain.Comments;
import java.time.LocalDateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface CommentRepository {

  Comments save(Comments comments);

  Slice<Comments> findSliceByAiSummaryId(Long aiSummaryId, LocalDateTime cursorCreatedAt,
      Long cursorId, Pageable pageable);

  long countByAiSummaryId(Long aiSummaryId);
}