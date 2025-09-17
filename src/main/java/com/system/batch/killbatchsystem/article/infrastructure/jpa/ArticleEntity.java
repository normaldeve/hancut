package com.system.batch.killbatchsystem.article.infrastructure.jpa;

import com.system.batch.killbatchsystem.article.domain.Article;
import com.system.batch.killbatchsystem.article.domain.SummarizeStatus;
import com.system.batch.killbatchsystem.article.infrastructure.batch.common.ArticleSource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
    name = "articles",
    uniqueConstraints = @UniqueConstraint(name = "uk_article_id", columnNames = "article_id")
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 64)
  private String articleId;

  @Column(nullable = false, length = 2048)
  private String url;

  @Column(columnDefinition = "text")
  private String content;

  // 카드 썸네일/요약/출처
  @Column(length = 2048)
  private String thumbnailUrl;

  @Column(length = 64)
  private ArticleSource sourceName;

  @Column(nullable = false)
  private LocalDateTime publishedAt;

  @CreatedDate
  private LocalDateTime createdAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "summarize_status", nullable = false)
  private SummarizeStatus summarizeStatus = SummarizeStatus.PENDING;

  public void completedSummary() {
    this.summarizeStatus = SummarizeStatus.DONE;
  }

  public void failedSummary() {
    this.summarizeStatus = SummarizeStatus.FAILED;
  }

  @Builder
  public ArticleEntity(String articleId, String url, String content, String thumbnailUrl,
      ArticleSource sourceName, LocalDateTime publishedAt, LocalDateTime createdAt,
      SummarizeStatus summarizeStatus) {
    this.articleId = articleId;
    this.url = url;
    this.content = content;
    this.thumbnailUrl = thumbnailUrl;
    this.sourceName = sourceName;
    this.publishedAt = publishedAt;
    this.createdAt = createdAt;
    this.summarizeStatus = summarizeStatus;
  }

  public static Article toModel(ArticleEntity entity) {
    return Article.builder()
        .id(entity.getId())
        .articleId(entity.getArticleId())
        .url(entity.getUrl())
        .content(entity.getContent())
        .thumbnailUrl(entity.getThumbnailUrl())
        .sourceName(entity.getSourceName())
        .publishedAt(entity.getPublishedAt())
        .createdAt(entity.getCreatedAt())
        .summarizeStatus(entity.getSummarizeStatus())
        .build();
  }

  public static ArticleEntity fromModel(Article article) {
    return ArticleEntity.builder()
        .articleId(article.articleId())
        .url(article.url())
        .content(article.content())
        .thumbnailUrl(article.thumbnailUrl())
        .sourceName(article.sourceName())
        .publishedAt(article.publishedAt())
        .createdAt(article.createdAt())
        .summarizeStatus(article.summarizeStatus())
        .build();
  }
}
