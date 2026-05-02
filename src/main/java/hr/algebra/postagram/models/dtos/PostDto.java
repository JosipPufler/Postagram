package hr.algebra.postagram.models.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PostDto {
    private Long id;
    private String description;
    private String image;
    private LocalDateTime postTime;
    private String author;
    private List<String> hashtags;
    private Double aspectRatio;
    private String storageType;
}
