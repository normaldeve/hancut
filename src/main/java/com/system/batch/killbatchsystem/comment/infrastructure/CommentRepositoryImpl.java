package com.system.batch.killbatchsystem.comment.infrastructure;

import com.system.batch.killbatchsystem.comment.domain.Comments;
import com.system.batch.killbatchsystem.comment.infrastructure.jpa.CommentEntity;
import com.system.batch.killbatchsystem.comment.infrastructure.jpa.CommentJpaRepository;
import com.system.batch.killbatchsystem.comment.application.CommentRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepository {

  private final CommentJpaRepository commentJpaRepository;

  @Override
  public Comments save(Comments comments) {
    CommentEntity commentEntity = CommentEntity.fromModel(comments);

    return CommentEntity.toModel(commentJpaRepository.save(commentEntity));
  }

  @Override
  public Slice<Comments> findSliceByAiSummaryId(Long aiSummaryId, LocalDateTime cursorCreatedAt,
      Long cursorId, Pageable pageable) {
    Slice<CommentEntity> slice = commentJpaRepository
        .findSliceByAiSummaryId(aiSummaryId, cursorCreatedAt, cursorId, pageable);

    return slice.map(CommentEntity::toModel);
  }

  @Override
  public long countByAiSummaryId(Long aiSummaryId) {
    return commentJpaRepository.countByAiSummaryId(aiSummaryId);
  }
}
