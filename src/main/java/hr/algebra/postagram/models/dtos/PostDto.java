package hr.algebra.postagram.models.dtos;

import hr.algebra.postagram.models.Hashtag;
import hr.algebra.postagram.models.Post;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Data
public class PostDto {
    public PostDto(Post post){
        id = post.getId();
        description = post.getDescription();
        author = post.getUser().getUsername();
        image = post.getImage() == null ? null : Base64.getEncoder().encodeToString(post.getImage());
        hashtags = post.getHashtags().stream().map(Hashtag::toString).toList();
        postTime = post.getPostedAt();
        aspectRatio = post.getAspectRatio();
    }

    private Long id;
    private String description;
    private String image;
    private LocalDateTime postTime;
    private String author;
    private List<String> hashtags;
    private Double aspectRatio;
}
