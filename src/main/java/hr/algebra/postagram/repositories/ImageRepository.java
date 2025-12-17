package hr.algebra.postagram.repositories;

import hr.algebra.postagram.models.Hashtag;
import hr.algebra.postagram.models.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ImageRepository extends JpaRepository<Image, UUID> {
}
