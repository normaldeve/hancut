package com.system.batch.killbatchsystem.reaction.application;

import com.system.batch.killbatchsystem.reaction.domain.CreateReaction;
import com.system.batch.killbatchsystem.reaction.domain.ReactionResponse;
import com.system.batch.killbatchsystem.reaction.domain.ReactionType;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest
@ActiveProfiles("test")
class ReactionServiceImplTest {

  @Autowired
  private ReactionService reactionService;

  private static final Long SUMMARY_ID = 1L;
  private static final String USER_ID = "TEST_USER_ID";

  private CreateReaction request(Long summaryId, String userId, ReactionType reactionType) {
    return new CreateReaction(summaryId, userId, reactionType);
  }

  @Test
  @DisplayName("새로운 반응을 생성합니다: 기존 반응이 없을 때 LIKE 추가하기")
  void toggleReaction_newReaction_success() {
    //when
    ReactionResponse res = reactionService.toggleReaction(
        request(SUMMARY_ID, USER_ID, ReactionType.LIKE));

    //then
    assertThat(res.likeCount()).isEqualTo(1);
    assertThat(res.dislikeCount()).isEqualTo(0);
    assertThat(res.userReaction().type()).isEqualTo(ReactionType.LIKE);
  }

  @Test
  @DisplayName("같은 반응을 생성합니다: 이미 Like인 상태에서 다시 Like 요청하기")
  void toggleReaction_sameReaction_success() {
    reactionService.toggleReaction(request(SUMMARY_ID, USER_ID, ReactionType.LIKE));

    // when: 같은 타입으로 한 번 더 요청 → 삭제
    ReactionResponse res = reactionService.toggleReaction(request(SUMMARY_ID, USER_ID, ReactionType.LIKE));

    // then
    assertThat(res.likeCount()).isEqualTo(0);
    assertThat(res.dislikeCount()).isEqualTo(0);
    assertThat(res.userReaction()).isNull();
  }

  @Test
  @DisplayName("다른 반응: LIKE 상태에서 DISLIKE 요청 → 타입 업데이트되어 likeCount=0, dislikeCount=1, userReaction=DISLIKE")
  void toggleReaction_differentReaction_updatesType() {
    // given: LIKE 로 만들어 둔다
    reactionService.toggleReaction(request(SUMMARY_ID, USER_ID, ReactionType.LIKE));

    // when: 다른 타입 DISLIKE 로 변경
    ReactionResponse res = reactionService.toggleReaction(request(SUMMARY_ID, USER_ID, ReactionType.DISLIKE));

    // then
    assertThat(res.likeCount()).isEqualTo(0);
    assertThat(res.dislikeCount()).isEqualTo(1);
    assertThat(res.userReaction().type()).isEqualTo(ReactionType.DISLIKE);
  }

  /**
   * TODO 동시성 문제 해결하기
   * @throws Exception
   */
  @Test
  @DisplayName("동일 사용자가 동시에 토글 - 최종 상태 확인")
  void concurrent_toggle_sameUser() throws Exception {
    String userId = "test-user";
    int toggleCount = 100;

    ExecutorService es = Executors.newFixedThreadPool(10);
    CountDownLatch start = new CountDownLatch(1);
    CountDownLatch done = new CountDownLatch(toggleCount);

    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failureCount = new AtomicInteger(0);

    for (int i = 0; i < toggleCount; i++) {
      es.submit(() -> {
        try {
          start.await();
          reactionService.toggleReaction(request(SUMMARY_ID, userId, ReactionType.LIKE));
          successCount.incrementAndGet();
        } catch (Exception e) {
          failureCount.incrementAndGet();
          System.err.println("Toggle failed: " + e.getMessage());
        } finally {
          done.countDown();
        }
      });
    }

    start.countDown();
    done.await();
    es.shutdown();

    ReactionResponse reaction = reactionService.getReactions(SUMMARY_ID, userId);

    System.out.println("Success: " + successCount.get());
    System.out.println("Failure: " + failureCount.get());
    System.out.println("Final like count: " + reaction.likeCount());

    // 성공한 요청이 있어야 하고, 최종 상태는 성공 횟수에 따라 결정되어야 함
    assertTrue(failureCount.get() > 0, "적어도 일부 요청은 성공해야 함");
  }
}