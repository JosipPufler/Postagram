package hr.algebra.postagram.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationForm {
    String username;
    String password;
    String confirmPassword;
    String email;
    Long packageId;
}
