package com.system.batch.killbatchsystem.comment.api;

import com.system.batch.killbatchsystem.comment.domain.Comments;
import com.system.batch.killbatchsystem.comment.domain.CreateComment;
import com.system.batch.killbatchsystem.comment.domain.GetComment;
import com.system.batch.killbatchsystem.comment.application.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comments/{aiSummaryId}")
@RequiredArgsConstructor
public class CommentController {

  private final CommentService commentService;

  @PostMapping
  public ResponseEntity<Comments> createComments(
      @PathVariable Long aiSummaryId,
      @Validated @RequestBody CreateComment createComment
  ) {
    Comments comments = commentService.create(aiSummaryId, createComment);

    return ResponseEntity.status(HttpStatus.CREATED).body(comments);
  }

  @GetMapping
  public ResponseEntity<GetComment> list(
      @PathVariable Long aiSummaryId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(required = false) Integer size
  ) {
    int pageSize = (size == null || size <= 0) ? 20 : Math.min(size, 100);

    GetComment result = commentService.getComments(aiSummaryId, page, pageSize);
    return ResponseEntity.ok(result);
  }
}
