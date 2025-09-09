package com.system.batch.killbatchsystem.comment.application;

import com.system.batch.killbatchsystem.comment.domain.Comments;
import com.system.batch.killbatchsystem.comment.domain.CreateComment;
import com.system.batch.killbatchsystem.comment.domain.GetComment;
import com.system.batch.killbatchsystem.summary.domain.AISummary;
import com.system.batch.killbatchsystem.summary.application.AISummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
  public GetComment getComments(Long aiSummaryId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<Comments> comments = commentRepository.findPageByAiSummaryId(aiSummaryId, pageable);

    return GetComment.of(comments);
  }
}
