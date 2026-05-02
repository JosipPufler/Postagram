package hr.algebra.postagram.helper;

import hr.algebra.postagram.models.Role;
import hr.algebra.postagram.models.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserHelper {
    public static final Role USER_ROLE = new Role("USER");
    public static final Role ADMIN_ROLE = new Role("ADMIN");

    public static Role getUserRole(){
        return new Role("USER");
    }

    public static User getDefaultUserWithId() {
        return User.builder()
                .id(1L)
                .username("user")
                .password("plaintextPassword")
                .active(true)
                .roles(List.of(USER_ROLE))
                .email("user@gmail.com")
                .userPackage(PackageHelper.getUserPackageWithId()).build();
    }

    public static User getDefaultUser() {
        return User.builder()
                .id(null)
                .username("user")
                .password("plaintextPassword")
                .active(true)
                .roles(List.of(USER_ROLE))
                .email("user@gmail.com")
                .userPackage(PackageHelper.getUserPackageWithId()).build();
    }
}
