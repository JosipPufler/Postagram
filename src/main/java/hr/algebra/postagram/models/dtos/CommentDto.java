package hr.algebra.postagram.models.dtos;

import java.time.LocalDateTime;

public record CommentDto(String username, String content, LocalDateTime postedAt) {
}
