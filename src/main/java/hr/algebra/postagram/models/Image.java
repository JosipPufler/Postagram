package hr.algebra.postagram.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "image")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Lob
    @JdbcTypeCode(java.sql.Types.BINARY)
    @Column(name = "image", columnDefinition = "BYTEA")
    private byte[] image;

    @Column(name = "content_type")
    private String contentType;
}
