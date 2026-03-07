package hr.algebra.postagram.models;

import lombok.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Builder
@Data
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {
    private final Long id;
    private final String email;
    private final String username;
    private final String password;
    private List<SimpleGrantedAuthority> authorities;

    public CustomUserDetails(User user){
        id = user.getId();
        username = user.getUsername();
        password = user.getPassword();
        email = user.getEmail();
        authorities = user.getRoles().stream().map(x -> new SimpleGrantedAuthority(STR."ROLE_\{x}")).toList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
