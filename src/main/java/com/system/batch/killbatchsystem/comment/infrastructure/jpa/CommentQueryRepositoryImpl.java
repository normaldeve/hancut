package com.system.batch.killbatchsystem.comment.infrastructure.jpa;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import static com.system.batch.killbatchsystem.comment.infrastructure.jpa.QCommentEntity.commentEntity;

@Repository
@RequiredArgsConstructor
public class CommentQueryRepositoryImpl implements CommentQueryRepository{

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<CommentEntity> findPageByAiSummaryId(Long aiSummaryId, Pageable pageable) {
    Long totalCount = queryFactory
        .select(commentEntity.count())
        .from(commentEntity)
        .where(commentEntity.aiSummaryId.eq(aiSummaryId))
        .fetchOne();
    long total = (totalCount == null) ? 0L : totalCount;

    List<CommentEntity> rows = queryFactory
        .selectFrom(commentEntity)
        .where(commentEntity.aiSummaryId.eq(aiSummaryId))
        .orderBy(commentEntity.createdAt.asc(), commentEntity.id.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    return new PageImpl<>(rows, pageable, total);
  }
}
