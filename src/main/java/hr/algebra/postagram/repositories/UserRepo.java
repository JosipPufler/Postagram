package hr.algebra.postagram.repositories;

import hr.algebra.postagram.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Page<User> findUsersByUsernameContaining(String username, Pageable pageable);
    Optional<User> findByEmail(String email);
}
