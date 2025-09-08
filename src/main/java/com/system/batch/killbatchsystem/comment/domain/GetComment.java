package com.system.batch.killbatchsystem.comment.domain;

import java.util.List;

public record GetComment(
    List<Comments> comments,
    CommentCursor nextCursor,
    boolean hasNext
) {

}
