package hr.algebra.postagram.models.dtos;

import jakarta.validation.constraints.Email;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class UserDto {
    private Long id;

    private String username;

    private String packageName;

    private Long packageId;

    @Email
    private String email;
}
