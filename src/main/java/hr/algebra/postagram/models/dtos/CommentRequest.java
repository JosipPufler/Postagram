package hr.algebra.postagram.models.dtos;

import jakarta.validation.constraints.NotBlank;

public record CommentRequest(@NotBlank String content) {
}
