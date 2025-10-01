package com.system.batch.killbatchsystem.summary.application;

import com.system.batch.killbatchsystem.common.exception.CustomException;
import com.system.batch.killbatchsystem.common.exception.ErrorCode;
import com.system.batch.killbatchsystem.model.ArticleSource;
import com.system.batch.killbatchsystem.model.GetAISummary;
import com.system.batch.killbatchsystem.model.PageResponseGetAISummary;
import com.system.batch.killbatchsystem.model.SortBy;
import com.system.batch.killbatchsystem.model.TopKeyword;
import com.system.batch.killbatchsystem.summary.domain.AISummary;
import com.system.batch.killbatchsystem.summary.domain.CreateSummary;
import com.system.batch.killbatchsystem.summary.domain.SummaryContent;
import jakarta.annotation.Nullable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SummaryServiceImpl implements SummaryService {

  private final AISummaryRepository aiSummaryRepository;

  private final AISummarize aisummarize;

  @Value("${keywords.top.max-limit:100}")
  private int maxLimit;

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void createAIArticle(CreateSummary createSummary) {
    SummaryContent summary = aisummarize.summarize(createSummary.content());

    AISummary aiSummary = AISummary.createSummary(summary, createSummary);

    aiSummaryRepository.save(aiSummary);
  }

  @Override
  @Transactional(readOnly = true)
  public AISummary findAISummaryById(Long id) {
    return aiSummaryRepository.findById(id)
        .orElseThrow(() -> new CustomException(ErrorCode.SUMMARY_NOT_FOUND));
  }

  @Override
  @Transactional(readOnly = true)
  public List<TopKeyword> topKeywords(int limit) {
    return aiSummaryRepository.findTopKeywords(topPage(limit));
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponseGetAISummary getArticles(
      @Nullable String keyword,
      @Nullable ArticleSource sourceName,
      Pageable pageable,
      SortBy sortBy
  ) {

    ArticleSource domainSource = null;
    if (sourceName != null) {
      domainSource = ArticleSource.valueOf(sourceName.getValue());
    }

    SortBy domainSort =
        (sortBy == null) ? SortBy.LATEST
            : SortBy.valueOf(sortBy.getValue());

    Page<AISummary> page = aiSummaryRepository.findPage(keyword, domainSource, pageable, domainSort);

    List<GetAISummary> cards = page.getContent()
        .stream()
        .map(this::toApi)
        .toList();

    PageResponseGetAISummary resp = new PageResponseGetAISummary();
    resp.setCards(cards);
    resp.setPage(page.getNumber());
    resp.setTotalPages(page.getTotalPages());
    resp.setLast(page.isLast());
    resp.setSize(page.getSize());
    resp.setNumberOfElements(page.getNumberOfElements());
    resp.setTotalElements(page.getTotalElements());
    return resp;
  }

  private Pageable topPage(int limit) {
    int size = Math.max(1, Math.min(limit, maxLimit));
    return PageRequest.of(0, size);
  }

  private GetAISummary toApi(AISummary s) {
    return new GetAISummary()
        .id(s.id())
        .title(s.title())
        .summary(s.summary())
        .keyword(s.keyword())
        .publishedAt(s.publishedAt())
        .thumbnailUrl(s.thumbnailUrl())
        .league(s.league())
        .team(s.team())
        .url(s.url())
        .sourceName(ArticleSource.fromValue(s.sourceName().name())
        );
  }
}