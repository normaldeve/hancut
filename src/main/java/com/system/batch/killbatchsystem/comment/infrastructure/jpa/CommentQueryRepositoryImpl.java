package com.system.batch.killbatchsystem.comment.infrastructure.jpa;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.micrometer.common.lang.Nullable;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import static com.system.batch.killbatchsystem.comment.infrastructure.jpa.QCommentEntity.commentEntity;

@Repository
@RequiredArgsConstructor
public class CommentQueryRepositoryImpl implements CommentQueryRepository{

  private final JPAQueryFactory queryFactory;

  @Override
  public Slice<CommentEntity> findSliceByAiSummaryId(Long aiSummaryId,
      LocalDateTime cursorCreatedAt, Long cursorId, Pageable pageable) {

    int size = pageable.getPageSize();

    List<CommentEntity> rows = queryFactory
        .selectFrom(commentEntity)
        .where(
            commentEntity.aiSummaryId.eq(aiSummaryId),
            cursorPredicate(cursorCreatedAt, cursorId)
        )
        .orderBy(
            commentEntity.createdAt.asc(),
            commentEntity.id.desc()
        )
        .limit(size + 1)
        .fetch();

    boolean hasNext = rows.size() > size;
    if (hasNext) {
      rows = rows.subList(0, size);
    }

    Sort sort = Sort.by(Sort.Order.asc("createdAt"), Sort.Order.desc("id"));
    PageRequest slicePageable = PageRequest.of(0, size, sort);

    return new SliceImpl<>(rows, slicePageable, hasNext);
  }

  private BooleanExpression cursorPredicate(
      @Nullable LocalDateTime cursorCreatedAt,
      @Nullable Long cursorId) {
    if (cursorCreatedAt == null || cursorId == null) {
      return null; // 첫 페이지: 커서 없음
    }
    return commentEntity.createdAt.lt(cursorCreatedAt)
        .or(
            commentEntity.createdAt.eq(cursorCreatedAt)
                .and(commentEntity.id.lt(cursorId))
        );
  }
}
