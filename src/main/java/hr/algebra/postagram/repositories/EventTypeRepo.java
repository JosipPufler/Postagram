package hr.algebra.postagram.repositories;

import hr.algebra.postagram.models.EventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventTypeRepo extends JpaRepository<EventType, Long> {
    Optional<EventType> findByName(String name);
}
