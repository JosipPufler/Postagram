package hr.algebra.postagram.helper;

import hr.algebra.postagram.models.Package;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PackageHelper {
    public static Package getUserPackageWithId(){
        return Package.builder()
                .id(1L)
                .name("default")
                .price(3.99)
                .maxUploads(10000)
                .maxUploadSize(11000L).build();
    }

    public static Package getUserPackage() {
        return Package.builder()
                .id(null)
                .name("default")
                .price(3.99)
                .maxUploads(10000)
                .maxUploadSize(11000L).build();
    }
}
