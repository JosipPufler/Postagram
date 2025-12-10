package hr.algebra.postagram.models.dtos;

import hr.algebra.postagram.models.Package;
import lombok.Data;

@Data
public class PackageDto {
    public PackageDto(Package blPackage) {
        id = blPackage.getId();
        name = blPackage.getName();
        description = "Max uploads: " + blPackage.getMaxUploads() + "\n" +
                "Max upload size: " + blPackage.getMaxUploadSize();
    }

    Long id;
    String name;
    String description;
}
