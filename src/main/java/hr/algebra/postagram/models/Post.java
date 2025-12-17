package hr.algebra.postagram.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
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

    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name = "post_hashtag",
            joinColumns = {@JoinColumn(name = "POST_ID", referencedColumnName = "ID")},
            inverseJoinColumns = {@JoinColumn(name = "HASHTAG_ID", referencedColumnName = "ID")}
    )
    Set<Hashtag> hashtags = new HashSet<>();

    @Column(name = "image_width")
    private Integer imageWidth;

    @Column(name = "image_height")
    private Integer imageHeight;

    @Column(name = "aspect_ratio")
    private Double aspectRatio;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "image_id", nullable = false)
    private String imageId;

    @Override
    public String toString(){
        return "Post by " + user.getUsername() + ": " + description + ", " + postedAt;
    }

    public void updateImageData(MultipartFile image) {
        try {
            BufferedImage buffered = ImageIO.read(image.getInputStream());
            imageWidth = buffered.getWidth();
            imageHeight = buffered.getHeight();
            aspectRatio = (double)imageWidth/imageHeight;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
