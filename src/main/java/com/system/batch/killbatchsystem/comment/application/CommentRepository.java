package com.system.batch.killbatchsystem.comment.application;

import com.system.batch.killbatchsystem.comment.domain.Comments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentRepository {

  Comments save(Comments comments);

  Page<Comments> findPageByAiSummaryId(Long aiSummaryId, Pageable pageable);
}