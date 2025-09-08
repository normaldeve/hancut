package com.system.batch.killbatchsystem.summary.infrastructure.jpa;

import com.system.batch.killbatchsystem.summary.domain.AISummary;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "ai_summary",
    indexes = {
        @Index(name = "idx_ai_summary_published_at", columnList = "publishedAt DESC")
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class AISummaryEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String title;

  private String league;

  private String team;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  private List<String> contents;

  @ElementCollection
  @CollectionTable(name = "ai_summary_keywords", joinColumns = @JoinColumn(name = "ai_summary_id"))
  private List<String> keyword;

  LocalDateTime publishedAt;

  String thumbnailUrl;

  String url;

  String sourceName;

  @Builder
  public AISummaryEntity(String title, String league, String team, List<String> contents,
      List<String> keyword, LocalDateTime publishedAt, String thumbnailUrl, String url, String sourceName) {
    this.title = title;
    this.league = league;
    this.team = team;
    this.contents = contents;
    this.keyword = keyword;
    this.publishedAt = publishedAt;
    this.thumbnailUrl = thumbnailUrl;
    this.url = url;
    this.sourceName = sourceName;
  }

  public static AISummary toModel(AISummaryEntity aiSummaryEntity) {
    return AISummary.builder()
        .id(aiSummaryEntity.id)
        .title(aiSummaryEntity.title)
        .league(aiSummaryEntity.league)
        .team(aiSummaryEntity.team)
        .summary(aiSummaryEntity.contents)
        .keyword(aiSummaryEntity.keyword)
        .publishedAt(aiSummaryEntity.publishedAt)
        .thumbnailUrl(aiSummaryEntity.thumbnailUrl)
        .url(aiSummaryEntity.url)
        .sourceName(aiSummaryEntity.sourceName)
        .build();
  }

  public static AISummaryEntity fromModel(AISummary aiSummary) {
    return AISummaryEntity.builder()
        .title(aiSummary.title())
        .league(aiSummary.league())
        .team(aiSummary.team())
        .contents(aiSummary.summary())
        .keyword(aiSummary.keyword())
        .publishedAt(aiSummary.publishedAt())
        .thumbnailUrl(aiSummary.thumbnailUrl())
        .url(aiSummary.url())
        .sourceName(aiSummary.sourceName())
        .build();
  }
}
