package com.system.batch.killbatchsystem.summary.infrastructure.jpa;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.system.batch.killbatchsystem.comment.infrastructure.jpa.QCommentEntity;
import com.system.batch.killbatchsystem.reaction.domain.ReactionType;
import com.system.batch.killbatchsystem.reaction.infrastructure.jpa.QReactionEntity;
import com.system.batch.killbatchsystem.summary.domain.TopKeyword;
import static com.system.batch.killbatchsystem.summary.infrastructure.jpa.QAISummaryEntity.aISummaryEntity;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AISummaryQueryRepositoryImpl implements AISummaryQueryRepository {

  private final JPAQueryFactory queryFactory;


  @Override
  public List<TopKeyword> findTopKeywords(Pageable pageable) {
    StringPath k = com.querydsl.core.types.dsl.Expressions.stringPath("k");

    StringExpression trimmed = k.trim();
    NumberExpression<Integer> len = trimmed.length();

    NumberExpression<Long> cnt = k.count();

    JPAQuery<TopKeyword> q = queryFactory
        .select(Projections.constructor(
            TopKeyword.class,
            k,
            cnt
        ))
        .from(aISummaryEntity)
        .join(aISummaryEntity.keyword, k)
        .where(
            k.isNotNull()
                .and(len.gt(0))
        )
        .groupBy(k)
        .orderBy(cnt.desc(), k.asc());

    if (pageable != null && pageable.isPaged()) {
      q.offset(pageable.getOffset());
      q.limit(pageable.getPageSize());
    }
    return q.fetch();
  }

  @Override
  public Page<AISummaryEntity> findPage(String keyword, String sourceName, Pageable pageable,
      SortBy sortBy) {
    BooleanBuilder where = new BooleanBuilder();

    if (keyword != null && !keyword.isBlank()) {
      where.and(aISummaryEntity.keyword.any().eq(keyword.trim()));
    }
    if (sourceName != null && !sourceName.isBlank()) {
      where.and(aISummaryEntity.sourceName.equalsIgnoreCase(sourceName.trim()));
    }

    Long total = queryFactory
        .select(aISummaryEntity.id.count())
        .from(aISummaryEntity)
        .where(where)
        .fetchOne();

    if (total == null || total == 0L) {
      return new PageImpl<>(Collections.emptyList(), pageable, 0);
    }

    // 1) 정렬 기준으로 먼저 ids 뽑기
    var idQuery = queryFactory
        .select(aISummaryEntity.id)
        .from(aISummaryEntity)
        .where(where);

    switch (sortBy) {
      case POPULAR -> idQuery.orderBy(
          popularityScore().desc(),
          aISummaryEntity.publishedAt.desc(),
          aISummaryEntity.id.desc()
      );
      case LATEST -> idQuery.orderBy(
          aISummaryEntity.publishedAt.desc(),
          aISummaryEntity.id.desc()
      );
    }

    List<Long> ids = idQuery
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    if (ids.isEmpty()) {
      return new PageImpl<>(Collections.emptyList(), pageable, 0);
    }

    // 2) 본문 로드 + ids 순서 보존
    List<AISummaryEntity> raw = queryFactory
        .selectFrom(aISummaryEntity)
        .where(aISummaryEntity.id.in(ids))
        .fetch();

    Map<Long, AISummaryEntity> byId = new LinkedHashMap<>();
    for (AISummaryEntity e : raw) byId.put(e.getId(), e);
    List<AISummaryEntity> content = ids.stream().map(byId::get).toList();

    return new PageImpl<>(content, pageable, total);
  }

  // ===== 가중치 합 스코어 구현 =====
  // 좋아요/싫어요/댓글 카운트 서브쿼리
  private NumberExpression<Long> likeCountExpr() {
    QReactionEntity r = QReactionEntity.reactionEntity;
    SubQueryExpression<Long> sub = JPAExpressions
        .select(r.id.count())
        .from(r)
        .where(r.aiSummaryId.eq(aISummaryEntity.id)
            .and(r.type.eq(ReactionType.LIKE)));

    return Expressions.numberTemplate(Long.class, "({0})", sub);
  }

  private NumberExpression<Long> dislikeCountExpr() {
    QReactionEntity r = QReactionEntity.reactionEntity;
    SubQueryExpression<Long> sub = JPAExpressions
        .select(r.id.count())
        .from(r)
        .where(r.aiSummaryId.eq(aISummaryEntity.id)
            .and(r.type.eq(ReactionType.DISLIKE)));

    return Expressions.numberTemplate(Long.class, "({0})", sub);
  }

  private NumberExpression<Long> commentCountExpr() {
    QCommentEntity c = QCommentEntity.commentEntity;
    SubQueryExpression<Long> sub = JPAExpressions
        .select(c.id.count())
        .from(c)
        .where(c.aiSummaryId.eq(aISummaryEntity.id));

    return Expressions.numberTemplate(Long.class, "({0})", sub);
  }

  /**
   * score = (likes*2) + comments - dislikes
   */
  private NumberExpression<Double> popularityScore() {
    NumberExpression<Long> likes = likeCountExpr().coalesce(0L);
    NumberExpression<Long> dislikes = dislikeCountExpr().coalesce(0L);
    NumberExpression<Long> comments = commentCountExpr().coalesce(0L);

    NumberExpression<Long> raw = likes.multiply(2L)
        .add(comments)
        .subtract(dislikes);

    return raw.castToNum(Double.class);
  }
}
