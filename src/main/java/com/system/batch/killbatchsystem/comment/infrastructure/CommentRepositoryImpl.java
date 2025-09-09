package com.system.batch.killbatchsystem.comment.infrastructure;

import com.system.batch.killbatchsystem.comment.domain.Comments;
import com.system.batch.killbatchsystem.comment.infrastructure.jpa.CommentEntity;
import com.system.batch.killbatchsystem.comment.infrastructure.jpa.CommentJpaRepository;
import com.system.batch.killbatchsystem.comment.application.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
  public Page<Comments> findPageByAiSummaryId(Long aiSummaryId, Pageable pageable) {
    Page<CommentEntity> page = commentJpaRepository
        .findPageByAiSummaryId(aiSummaryId, pageable);

    return page.map(CommentEntity::toModel);
  }
}
