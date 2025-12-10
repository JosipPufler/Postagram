package hr.algebra.postagram.models.dtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostSearchForm {
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private List<String> hashtags;
    private String authorName;
    private String aspectRatio;
}
