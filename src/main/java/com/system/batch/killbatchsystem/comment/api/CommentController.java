package com.system.batch.killbatchsystem.comment.api;

import com.system.batch.killbatchsystem.comment.domain.CommentCursor;
import com.system.batch.killbatchsystem.comment.domain.Comments;
import com.system.batch.killbatchsystem.comment.domain.CreateComment;
import com.system.batch.killbatchsystem.comment.domain.GetComment;
import com.system.batch.killbatchsystem.comment.application.CommentService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.server.ResponseStatusException;

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
      @RequestParam(required = false) Integer size,
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorCreatedAt,
      @RequestParam(required = false) Long cursorId
  ) {
    CommentCursor cursor = null;
    if (cursorCreatedAt != null || cursorId != null) {
      if (cursorCreatedAt == null || cursorId == null) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Both 'cursorCreatedAt' and 'cursorId' are required together."
        );
      }
      cursor = new CommentCursor(cursorCreatedAt, cursorId);
    }

    GetComment result = commentService.getComments(aiSummaryId, size, cursor);
    return ResponseEntity.ok(result);
  }
}
