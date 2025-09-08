package com.system.batch.killbatchsystem.comment.application;

import com.system.batch.killbatchsystem.comment.domain.CommentCursor;
import com.system.batch.killbatchsystem.comment.domain.Comments;
import com.system.batch.killbatchsystem.comment.domain.CreateComment;
import com.system.batch.killbatchsystem.comment.domain.GetComment;
import com.system.batch.killbatchsystem.summary.domain.AISummary;
import com.system.batch.killbatchsystem.summary.application.AISummaryRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

  private final CommentRepository commentRepository;

  private final AISummaryRepository summaryRepository;

  @Override
  @Transactional
  public Comments create(Long aiSummaryId, CreateComment createComment) {
    AISummary aiSummary = summaryRepository.findById(aiSummaryId)
        .orElseThrow(() -> new RuntimeException("Can not found AI summary : {}" + aiSummaryId));

    Comments comments = Comments.createComment(createComment, aiSummary);

    return commentRepository.save(comments);
  }

  @Override
  @Transactional(readOnly = true)
  public GetComment getComments(Long aiSummaryId, Integer size, CommentCursor cursor) {
    int pageSize = (size == null || size <= 0) ? 20 : Math.min(size, 100);

    LocalDateTime cursorCreatedAt = (cursor == null) ? null : cursor.createdAt();
    Long cursorId = (cursor == null) ? null : cursor.id();

    Pageable pageable = PageRequest.of(0, pageSize);

    Slice<Comments> slice =
        commentRepository.findSliceByAiSummaryId(aiSummaryId, cursorCreatedAt, cursorId, pageable);

    CommentCursor nextCursor = null;
    if (slice.hasNext() && slice.getNumberOfElements() > 0) {
      Comments last = slice.getContent().get(slice.getNumberOfElements() - 1);
      nextCursor = new CommentCursor(last.createdAt(), last.id());
    }

    return new GetComment(
        slice.getContent(),
        nextCursor,
        slice.hasNext()
    );
  }
}
