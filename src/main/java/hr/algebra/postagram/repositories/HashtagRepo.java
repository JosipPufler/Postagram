package hr.algebra.postagram.repositories;

import hr.algebra.postagram.models.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HashtagRepo extends JpaRepository<Hashtag, Long> {
    Optional<Hashtag> findByName(String name);
}
