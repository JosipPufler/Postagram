package hr.algebra.postagram.repositories;

import hr.algebra.postagram.models.Event;
import hr.algebra.postagram.models.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EventRepo extends JpaRepository<Event, Long> {
    List<Event> findByEventType(EventType eventType);

    @Query(value = """
        SELECT e.*
        FROM events e
        JOIN event_types et ON e.event_type_id = et.id
        JOIN users u ON u.id = e.user_id
        WHERE u.username LIKE CONCAT('%', :username, '%')
        ORDER BY e.time DESC
    """,
    countQuery = """
        SELECT COUNT(DISTINCT E.id) FROM events e
        JOIN event_types et ON e.event_type_id = et.id
        JOIN users u ON u.id = e.user_id
        WHERE u.username LIKE CONCAT('%', :username, '%')
    """, nativeQuery = true)
    Page<Event> findPaged(String username, Pageable pageable);
}
