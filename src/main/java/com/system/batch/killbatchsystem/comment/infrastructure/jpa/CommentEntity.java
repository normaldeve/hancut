package com.system.batch.killbatchsystem.comment.infrastructure.jpa;

import com.system.batch.killbatchsystem.comment.domain.Comments;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "comments",
    indexes = {
        @Index(name = "idx_comments_created_at", columnList = "createdAt")
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class CommentEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long aiSummaryId;

  @Column(nullable = false)
  private String author;

  @Column(nullable = false)
  private String content;

  @CreatedDate
  private LocalDateTime createdAt;

  @Builder
  public CommentEntity(Long aiSummaryId, String author, String content, LocalDateTime createdAt) {
    this.aiSummaryId = aiSummaryId;
    this.author = author;
    this.content = content;
    this.createdAt = createdAt;
  }

  public static Comments toModel(CommentEntity commentEntity) {
    return Comments.builder()
        .id(commentEntity.id)
        .aiSummaryId(commentEntity.aiSummaryId)
        .author(commentEntity.author)
        .content(commentEntity.content)
        .createdAt(commentEntity.createdAt)
        .build();
  }

  public static CommentEntity fromModel(Comments comments) {
    return CommentEntity.builder()
        .aiSummaryId(comments.aiSummaryId())
        .author(comments.author())
        .content(comments.content())
        .build();
  }

}
