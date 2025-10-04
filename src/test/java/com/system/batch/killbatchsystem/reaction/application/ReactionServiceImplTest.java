//package com.system.batch.killbatchsystem.reaction.application;
//
//import com.system.batch.killbatchsystem.reaction.domain.CreateReaction;
//import com.system.batch.killbatchsystem.reaction.domain.ReactionResponse;
//import com.system.batch.killbatchsystem.reaction.domain.ReactionType;
//import static org.assertj.core.api.Assertions.*;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//
//
//@SpringBootTest
//@ActiveProfiles("test")
//class ReactionServiceImplTest {
//
//  @Autowired
//  private ReactionService reactionService;
//
//  private static final Long SUMMARY_ID = 1L;
//  private static final String USER_ID = "TEST_USER_ID";
//
//  private CreateReaction request(Long summaryId, String userId, ReactionType reactionType) {
//    return new CreateReaction(summaryId, userId, reactionType);
//  }
//
//  @Test
//  @DisplayName("새로운 반응을 생성합니다: 기존 반응이 없을 때 LIKE 추가하기")
//  void toggleReaction_newReaction_success() {
//    //when
//    ReactionResponse res = reactionService.toggleReaction(
//        request(SUMMARY_ID, USER_ID, ReactionType.LIKE));
//
//    //then
//    assertThat(res.likeCount()).isEqualTo(1);
//    assertThat(res.dislikeCount()).isEqualTo(0);
//    assertThat(res.userReaction().type()).isEqualTo(ReactionType.LIKE);
//  }
//
//  @Test
//  @DisplayName("같은 반응을 생성합니다: 이미 Like인 상태에서 다시 Like 요청하기")
//  void toggleReaction_sameReaction_success() {
//    reactionService.toggleReaction(request(SUMMARY_ID, USER_ID, ReactionType.LIKE));
//
//    // when: 같은 타입으로 한 번 더 요청 → 삭제
//    ReactionResponse res = reactionService.toggleReaction(request(SUMMARY_ID, USER_ID, ReactionType.LIKE));
//
//    // then
//    assertThat(res.likeCount()).isEqualTo(0);
//    assertThat(res.dislikeCount()).isEqualTo(0);
//    assertThat(res.userReaction()).isNull();
//  }
//
//  @Test
//  @DisplayName("다른 반응: LIKE 상태에서 DISLIKE 요청 → 타입 업데이트되어 likeCount=0, dislikeCount=1, userReaction=DISLIKE")
//  void toggleReaction_differentReaction_updatesType() {
//    // given: LIKE 로 만들어 둔다
//    reactionService.toggleReaction(request(SUMMARY_ID, USER_ID, ReactionType.LIKE));
//
//    // when: 다른 타입 DISLIKE 로 변경
//    ReactionResponse res = reactionService.toggleReaction(request(SUMMARY_ID, USER_ID, ReactionType.DISLIKE));
//
//    // then
//    assertThat(res.likeCount()).isEqualTo(0);
//    assertThat(res.dislikeCount()).isEqualTo(1);
//    assertThat(res.userReaction().type()).isEqualTo(ReactionType.DISLIKE);
//  }
//}