package com.system.batch.killbatchsystem.summary.infrastructure;

import com.system.batch.killbatchsystem.model.ArticleSource;
import com.system.batch.killbatchsystem.model.SortBy;
import com.system.batch.killbatchsystem.model.TopKeyword;
import com.system.batch.killbatchsystem.summary.domain.AISummary;
import com.system.batch.killbatchsystem.summary.infrastructure.jpa.AISummaryEntity;
import com.system.batch.killbatchsystem.summary.infrastructure.jpa.AISummaryJpaRepositoryImpl;
import com.system.batch.killbatchsystem.summary.application.AISummaryRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AISummaryRepositoryImpl implements AISummaryRepository {

  private final AISummaryJpaRepositoryImpl aiSummaryJpaRepository;

  @Override
  public AISummary save(AISummary aiSummary) {
    AISummaryEntity aiSummaryEntity = AISummaryEntity.fromModel(aiSummary);
    AISummaryEntity save = aiSummaryJpaRepository.save(aiSummaryEntity);

    return AISummaryEntity.toModel(save);
  }

  @Override
  public Optional<AISummary> findById(Long id) {
    return aiSummaryJpaRepository.findById(id)
        .map(AISummaryEntity::toModel);
  }

  @Override
  public List<TopKeyword> findTopKeywords(Pageable pageable) {
    return aiSummaryJpaRepository.findTopKeywords(pageable);
  }

  @Override
  public Page<AISummary> findPage(String keyword, ArticleSource sourceName, Pageable pageable, SortBy sortBy) {
    return aiSummaryJpaRepository.findPage(keyword, sourceName, pageable, sortBy)
        .map(AISummaryEntity::toModel);
  }
}
