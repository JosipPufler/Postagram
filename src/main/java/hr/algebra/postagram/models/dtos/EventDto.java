package hr.algebra.postagram.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventDto {
    private Long id;
    private String eventType;
    private String username;
    private Long userId;
    private String description;
    private LocalDateTime createdAt;
}
