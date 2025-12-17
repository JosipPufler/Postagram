package hr.algebra.postagram.repositories;

import hr.algebra.postagram.models.Hashtag;
import hr.algebra.postagram.models.Post;
import hr.algebra.postagram.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepo extends JpaRepository<Post, Long> {
    Page<Post> findByUser(User user, Pageable pageable);
    List<Post> findAll(Specification<Post> specification);

    @Query(value = "SELECT * FROM posts WHERE id NOT IN (:excludeIds) ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Post> findRandomPostsExcluding(@Param("excludeIds") List<Long> excludeIds, @Param("limit") int limit);

    @Query(value = """
    SELECT p.*
    FROM posts p
    JOIN post_hashtag ph ON p.id = ph.post_id
    JOIN hashtags h ON h.id = ph.hashtag_id
    WHERE h.name IN (:hashtags)
    GROUP BY p.id
    HAVING COUNT(DISTINCT h.name) = :size
""", nativeQuery = true)
    List<Post> findPostsByHashtags(
            @Param("hashtags") List<String> hashtags,
            @Param("size") int size
    );

    @Query(value = """
    SELECT DISTINCT p.*
    FROM posts p
    JOIN post_hashtag ph ON p.id = ph.post_id
    JOIN hashtags h ON h.id = ph.hashtag_id
    ORDER BY p.posted_at
""", nativeQuery = true)
    Page<Post> getLatest(Pageable pageable);

    @Query(value = """
    SELECT p.*
    FROM posts p
    JOIN post_hashtag ph ON p.id = ph.post_id
    JOIN hashtags h ON h.id = ph.hashtag_id
    JOIN users u ON u.id = p.user_id
    WHERE u.username LIKE CONCAT('%', :username, '%')
      AND p.posted_at BETWEEN :startTime AND :endTime
      AND h.name IN (:hashtags)
      AND (
        :ratioGroup IS NULL
        OR (
            (:ratioGroup = 'square' AND p.aspect_ratio BETWEEN 0.8 AND 1.2)
            OR (:ratioGroup = 'tall' AND p.aspect_ratio < 0.8)
            OR (:ratioGroup = 'wide' AND p.aspect_ratio > 1.2)
        )
    )
    GROUP BY p.id
    HAVING COUNT(DISTINCT h.name) = :#{#hashtags.size()}
    ORDER BY p.posted_at
""", nativeQuery = true)
    List<Post> getBySearchParams(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("username") String username,
            @Param("ratioGroup") String ratioGroup,
            @Param("hashtags") List<String> hashtags
    );

    @Query(value = """
    SELECT p.*
    FROM posts p
    JOIN post_hashtag ph ON p.id = ph.post_id
    JOIN hashtags h ON h.id = ph.hashtag_id
    JOIN users u ON u.id = p.user_id
    WHERE u.username LIKE CONCAT('%', :username, '%')
      AND p.posted_at BETWEEN :startTime AND :endTime
      AND (
        :ratioGroup IS NULL
        OR (
            (:ratioGroup = 'square' AND p.aspect_ratio BETWEEN 0.8 AND 1.2)
            OR (:ratioGroup = 'tall' AND p.aspect_ratio < 0.8)
            OR (:ratioGroup = 'wide' AND p.aspect_ratio > 1.2)
        )
    ORDER BY p.posted_at
""", nativeQuery = true)
    List<Post> getBySearchParams(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("username") String username,
            @Param("ratioGroup") String ratioGroup
    );
}
