package com.system.batch.killbatchsystem.user.infrastructure.jpa;

import static com.system.batch.killbatchsystem.user.infrastructure.jpa.QUserEntity.userEntity;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.system.batch.killbatchsystem.summary.domain.UserSearchCond;
import com.system.batch.killbatchsystem.user.domain.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class UserQueryRepositoryImpl implements UserQueryRepository{

  private final JPAQueryFactory query;


  @Override
  public Page<User> search(UserSearchCond cond, Pageable pageable) {
    BooleanBuilder where = new BooleanBuilder();

    if (StringUtils.hasText(cond.nickname())) {
      where.and(userEntity.nickname.containsIgnoreCase(cond.nickname()));
    }

    if (cond.role() != null) {
      where.and(userEntity.role.eq(cond.role()));
    }

    List<UserEntity> rows = query
        .selectFrom(userEntity)
        .where(where)
        .orderBy(userEntity.createdAt.desc(), userEntity.id.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    Long total = query
        .select(userEntity.id.count())
        .from(userEntity)
        .where(where)
        .fetchOne();

    List<User> content = rows.stream()
        .map(UserEntity::toModel)
        .toList();

    return new PageImpl<>(content, pageable, total == null ? 0 : total);
  }
}
