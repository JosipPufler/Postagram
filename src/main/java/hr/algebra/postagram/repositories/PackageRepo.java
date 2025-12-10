package hr.algebra.postagram.repositories;

import hr.algebra.postagram.models.Package;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackageRepo extends JpaRepository<Package, Long> {
}
