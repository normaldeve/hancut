package com.system.batch.killbatchsystem.reaction.application;

import com.system.batch.killbatchsystem.reaction.domain.CreateReaction;
import com.system.batch.killbatchsystem.reaction.domain.Reaction;
import com.system.batch.killbatchsystem.reaction.domain.ReactionResponse;
import com.system.batch.killbatchsystem.reaction.domain.ReactionType;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReactionServiceImpl implements ReactionService {

  private final ReactionRepository reactionRepository;

  @Override
  @Transactional(readOnly = true)
  public ReactionResponse getReactions(Long aiSummaryId, String userId) {
    List<Reaction> reactions = reactionRepository.findByAiSummaryId(aiSummaryId);

    Optional<Reaction> userReaction = reactionRepository.findByAiSummaryIdAndUserId(
        aiSummaryId, userId);

    long likeCount = reactions.stream()
        .filter(r -> r.type() == ReactionType.LIKE)
        .count();

    long dislikeCount = reactions.stream()
        .filter(r -> r.type() == ReactionType.DISLIKE)
        .count();

    return ReactionResponse.builder()
        .likeCount(likeCount)
        .dislikeCount(dislikeCount)
        .userReaction(userReaction.orElse(null))
        .build();
  }

  @Override
  @Transactional
  public ReactionResponse toggleReaction(CreateReaction request) {
    Long aiSummaryId = request.aiSummaryId();
    String userId = request.userId();
    ReactionType type = request.type();

    // 먼저 기존 반응을 조회
    Optional<Reaction> existingReaction = reactionRepository.findByAiSummaryIdAndUserId(
        aiSummaryId, userId);

    if (existingReaction.isPresent()) {
      Reaction reaction = existingReaction.get();

      if (reaction.type() == type) {
        log.info("같은 반응입니다. 이전 반응을 삭제합니다.");
        reactionRepository.delete(reaction);
      } else {
        log.info("다른 반응입니다. 반응을 업데이트합니다.");
        reactionRepository.updateType(type, aiSummaryId, userId);
      }
    } else {
      log.info("새로운 반응을 생성합니다");
      Reaction newReaction = Reaction.createReaction(request);
      reactionRepository.save(newReaction);
    }

    return buildReactionResponse(aiSummaryId, userId, existingReaction, type);
  }

  private ReactionResponse buildReactionResponse(Long aiSummaryId, String userId,
      Optional<Reaction> existingReaction,
      ReactionType newType) {
    // 현재 상태를 기반으로 카운트 계산
    long currentLikeCount = reactionRepository.countByAiSummaryIdAndType(aiSummaryId, ReactionType.LIKE);
    long currentDislikeCount = reactionRepository.countByAiSummaryIdAndType(aiSummaryId, ReactionType.DISLIKE);

    Optional<Reaction> currentUserReaction = reactionRepository.findByAiSummaryIdAndUserId(aiSummaryId, userId);

    return ReactionResponse.builder()
        .likeCount(currentLikeCount)
        .dislikeCount(currentDislikeCount)
        .userReaction(currentUserReaction.orElse(null))
        .build();
  }
}
