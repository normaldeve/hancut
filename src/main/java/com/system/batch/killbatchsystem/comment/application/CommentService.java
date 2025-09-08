package com.system.batch.killbatchsystem.comment.application;

import com.system.batch.killbatchsystem.comment.domain.CommentCursor;
import com.system.batch.killbatchsystem.comment.domain.Comments;
import com.system.batch.killbatchsystem.comment.domain.CreateComment;
import com.system.batch.killbatchsystem.comment.domain.GetComment;

public interface CommentService {

  Comments create(Long aiSummaryId, CreateComment createComment);

  GetComment getComments(Long aiSummaryId, Integer size, CommentCursor cursor);
}
