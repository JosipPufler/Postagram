package hr.algebra.postagram.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "packages")
public class Package {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    @Column(unique = true, nullable = false)
    private String name;

    @NotEmpty
    @Column(nullable = false)
    private Integer maxUploads;

    @NotEmpty
    @Column(nullable = false)
    private Integer maxUploadSize;

    @NotEmpty
    @Column(nullable = false)
    private Double price;

    @Override
    public String toString() {
        return name;
    }
}
