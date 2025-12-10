package hr.algebra.postagram.services;

import hr.algebra.postagram.models.Role;
import hr.algebra.postagram.models.RoleEnum;
import hr.algebra.postagram.repositories.RoleRepo;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService extends GeneralCrudService<Role, RoleRepo> {
    public RoleService(RoleRepo repository) {
        super(repository);
    }

    public Optional<Role> findByName(String name) {
        return repository.findByName(name);
    }

    public Role findByEnum(RoleEnum roleEnum) {
        Optional<Role> byName = repository.findByName(roleEnum.name());
        return byName.orElseGet(() -> repository.save(new Role(roleEnum.name())));
    }
}
