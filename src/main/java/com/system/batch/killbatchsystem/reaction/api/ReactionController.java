package com.system.batch.killbatchsystem.reaction.api;

import com.system.batch.killbatchsystem.reaction.domain.CreateReaction;
import com.system.batch.killbatchsystem.reaction.domain.ReactionResponse;
import com.system.batch.killbatchsystem.reaction.application.ReactionService;
import com.system.batch.killbatchsystem.user.security.infrastructure.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reactions")
@RequiredArgsConstructor
public class ReactionController {

  private final ReactionService reactionService;

  @GetMapping
  public ResponseEntity<ReactionResponse> getReactions(
      @RequestParam Long aiSummaryId,
      Authentication authentication
  ) {
    AuthUser userDetails = (AuthUser) authentication.getPrincipal();
    String username = userDetails.getUsername();
    ReactionResponse body = reactionService.getReactions(aiSummaryId, username);

    return ResponseEntity.ok(body);
  }

  @PostMapping
  public ResponseEntity<ReactionResponse> createReaction(
      @Validated @RequestBody CreateReaction createReaction,
      Authentication authentication
  ) {
    AuthUser userDetails = (AuthUser) authentication.getPrincipal();
    String username = userDetails.getUsername();
    CreateReaction changedUserId = createReaction.changeUserId(username);
    ReactionResponse body = reactionService.toggleReaction(changedUserId);

    return ResponseEntity.ok(body);
  }

}
