package hr.algebra.postagram.models;

import hr.algebra.postagram.models.dtos.PostForm;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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

    @Column(unique = true)
    private String email;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Builder.Default
    @Column(nullable = false)
    private Long uploadedAmount = 0L;

    public Double getUploadedAmountInMb() {
        return (double)Math.round(uploadedAmount/100000.0) / 10;
    }

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
    private Package userPackage;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "next_package_id")
    private Package nextPackage;

    private LocalDateTime nextPackageActivationTime;

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

    public void changePackage(Package newPackage) {
        nextPackage = newPackage;
        nextPackageActivationTime = LocalDateTime.now().plusDays(1);

        adjustToPackage();
    }

    public void adjustToPackage() {
        if (uploadCount > userPackage.getMaxUploads()){
            uploadCount= userPackage.getMaxUploads();
        }

        if (uploadedAmount > userPackage.getMaxUploadSize()){
            uploadedAmount = userPackage.getMaxUploadSize();
        }
    }
}
