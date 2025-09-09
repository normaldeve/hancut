package com.system.batch.killbatchsystem.comment.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentJpaRepository extends JpaRepository<CommentEntity, Long>, CommentQueryRepository {

}
