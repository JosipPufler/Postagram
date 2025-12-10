package hr.algebra.postagram.services;

import hr.algebra.postagram.models.Package;
import hr.algebra.postagram.repositories.PackageRepo;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class PackageService extends GeneralCrudService<Package, PackageRepo> {
    public PackageService(PackageRepo repository) {
        super(repository);
    }
}
