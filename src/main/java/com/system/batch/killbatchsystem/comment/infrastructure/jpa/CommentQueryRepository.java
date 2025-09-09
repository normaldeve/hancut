package com.system.batch.killbatchsystem.comment.infrastructure.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentQueryRepository {

  Page<CommentEntity> findPageByAiSummaryId(Long aiSummaryId, Pageable pageable);
}
