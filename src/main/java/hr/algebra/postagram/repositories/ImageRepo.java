package hr.algebra.postagram.repositories;

import hr.algebra.postagram.models.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepo extends JpaRepository<Image, String> {
}
