package hr.algebra.postagram.services;

import hr.algebra.postagram.models.Package;
import hr.algebra.postagram.models.RoleEnum;
import hr.algebra.postagram.models.User;
import hr.algebra.postagram.models.dtos.UserDto;
import hr.algebra.postagram.repositories.UserRepo;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService extends GeneralCrudService<User, UserRepo>{
    private final PackageService packageService;
    private final RoleService roleService;

    public UserService(UserRepo userRepo, PackageService packageService, RoleService roleService) {
        super(userRepo);
        this.packageService = packageService;
        this.roleService = roleService;
    }

    public Optional<User> findByUsername(String username) {
        return repository.findByUsername(username);
    }

    public Page<User> findByUsernamePaged(String username, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        return repository.findUsersByUsernameContaining(username, pageable);
    }

    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public Boolean usernameExists(String username) {
        return findByUsername(username).isPresent();
    }

    @Transactional
    public User deactivateUser(User user) {
        user.setActive(false);
        return repository.save(user);
    }

    @Transactional
    public User activateUser(User user) {
        user.setActive(true);
        return repository.save(user);
    }

    @Transactional
    public void registerOauthUser(Long packageId, String username) {
        Optional<Package> byId = packageService.findById(packageId);
        if (byId.isEmpty())
            return;
        User build = User.builder().userPackage(byId.get()).username(username).password("").roles(List.of(roleService.findByEnum(RoleEnum.USER))).build();
        repository.save(build);
    }

    public void saveFromDto(UserDto userDto) {
        Optional<User> userById = repository.findById(userDto.getId());
        Optional<Package> packageById = packageService.findById(userDto.getPackageId());
        if (userById.isEmpty() || packageById.isEmpty())
            return;

        User user = userById.get();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setUserPackage(packageById.get());
        repository.save(user);
    }
}
