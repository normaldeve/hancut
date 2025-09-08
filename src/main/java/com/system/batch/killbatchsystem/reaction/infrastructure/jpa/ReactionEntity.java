package com.system.batch.killbatchsystem.reaction.infrastructure.jpa;

import com.system.batch.killbatchsystem.reaction.domain.Reaction;
import com.system.batch.killbatchsystem.reaction.domain.ReactionType;
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

@Getter
@Entity
@Table(name = "reactions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"ai_summary_id", "user_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ReactionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "ai_summary_id", nullable = false)
  private Long aiSummaryId;

  @Column(name = "user_id", nullable = false)
  private String userId;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private ReactionType type;

  @CreatedDate
  private LocalDateTime createdAt;

  @Builder
  public ReactionEntity(Long aiSummaryId, String userId, ReactionType type) {
    this.aiSummaryId = aiSummaryId;
    this.userId = userId;
    this.type = type;
  }

  public void changeType(ReactionType type) {
    this.type = type;
  }

  public static Reaction toModel(ReactionEntity reactionEntity) {
    return Reaction.builder()
        .id(reactionEntity.id)
        .aiSummaryId(reactionEntity.aiSummaryId)
        .userId(reactionEntity.userId)
        .type(reactionEntity.type)
        .createdAt(reactionEntity.createdAt)
        .build();
  }

  public static ReactionEntity fromModel(Reaction reaction) {
    return ReactionEntity.builder()
        .aiSummaryId(reaction.aiSummaryId())
        .userId(reaction.userId())
        .type(reaction.type())
        .build();
  }
}