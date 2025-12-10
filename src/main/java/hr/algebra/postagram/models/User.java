package hr.algebra.postagram.models;

import hr.algebra.postagram.models.dtos.PostForm;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = true)
    private String email;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Builder.Default
    @Column(nullable = false)
    private Long uploadedAmount = 0L;

    @Builder.Default
    @Column(nullable = false)
    private Integer uploadCount = 0;

    @ManyToMany(fetch = FetchType.EAGER, cascade=CascadeType.ALL)
    @JoinTable(
            name="users_roles",
            joinColumns={@JoinColumn(name="USER_ID", referencedColumnName="ID")},
            inverseJoinColumns={@JoinColumn(name="ROLE_ID", referencedColumnName="ID")})
    private List<Role> roles = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "package_id", nullable = false)
    Package userPackage;

    public boolean canPublishPost(PostForm post){
        return uploadCount < userPackage.getMaxUploads() && userPackage.getMaxUploadSize() >= post.getImage().getSize();
    }

    public void publishPost(PostForm post){
        uploadCount++;
        uploadedAmount += post.getImage().getSize();
    }

    @Override
    public String toString() {
        return username;
    }
}
