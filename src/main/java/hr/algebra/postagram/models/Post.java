package hr.algebra.postagram.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotEmpty
    @Column(name = "description")
    String description;

    @NotNull
    @Column(name = "posted_at")
    LocalDateTime postedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "post_hashtag",
            joinColumns = {@JoinColumn(name = "POST_ID", referencedColumnName = "ID")},
            inverseJoinColumns = {@JoinColumn(name = "HASHTAG_ID", referencedColumnName = "ID")}
    )
    Set<Hashtag> hashtags = new HashSet<>();

    @Lob
    @JdbcTypeCode(java.sql.Types.BINARY)
    @Column(name = "image", columnDefinition = "BYTEA")
    private byte[] image;

    @Column(name = "image_width")
    private Integer imageWidth;

    @Column(name = "image_height")
    private Integer imageHeight;

    @Column(name = "aspect_ratio")
    private Double aspectRatio;

    @Column(name = "image_content_type")
    private String imageContentType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Override
    public String toString(){
        return imageContentType + " by " + user.getUsername() + ": " + description + ", " + postedAt;
    }
}
