package hr.algebra.postagram.models.specifications;

import hr.algebra.postagram.models.Post;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class PostSpecification {
    private PostSpecification(){}

    public static final Double WIDE_BOUND = 1.2;
    public static final Double TALL_BOUND = 0.8;
    public static final String ASPECT_RATIO = "aspectRatio";


    public static Specification<Post> isAuthorLike(String author){
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("user").get("username")), "%"+author.toLowerCase()+"%");
    }

    public static Specification<Post> isAspectRatioLike(String aspectRatio){
        return switch (aspectRatio.toLowerCase()) {
            case "square" -> (root, query, cb) ->
                    cb.between(root.get("post").get(ASPECT_RATIO), TALL_BOUND, WIDE_BOUND);
            case "wide" -> (root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("post").get(ASPECT_RATIO), WIDE_BOUND);
            case "tall" -> (root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("post").get(ASPECT_RATIO), TALL_BOUND);
            default -> null;
        };
    }

    public static Specification<Post> wasPostedBetween(LocalDateTime rangeStart, LocalDateTime rangeEnd){
        return (root, query, cb) -> cb.between(root.get("postedAt"), rangeStart, rangeEnd);
    }
}
