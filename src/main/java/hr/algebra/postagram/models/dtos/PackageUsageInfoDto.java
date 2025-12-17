package hr.algebra.postagram.models.dtos;

import hr.algebra.postagram.models.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageUsageInfoDto {
    public PackageUsageInfoDto(User user){
        maxUploadAmount = user.getUserPackage().getMaxUploadSizeInMb();
        currentUploadAmount = user.getUploadedAmountInMb();

        maxUploads = user.getUserPackage().getMaxUploads();
        currentUploads = user.getUploadCount();
    }

    private Integer maxUploads;
    private Integer currentUploads;
    private Double maxUploadAmount;
    private Double currentUploadAmount;
}
