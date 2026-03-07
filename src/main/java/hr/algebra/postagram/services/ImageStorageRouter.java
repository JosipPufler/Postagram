package hr.algebra.postagram.services;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ImageStorageRouter {
    @Getter
    @Value("${image.storage-type.write}")
    private String storageType;

    private final LocalImageService localImageService;
    private final BucketImageService bucketImageService;

    public ImageStorageRouter(LocalImageService localImageService, BucketImageService bucketImageService) {
        this.localImageService = localImageService;
        this.bucketImageService = bucketImageService;
    }

    public ImageService getWriter() {
        return switch (storageType.toLowerCase()) {
            case "db-storage" -> localImageService;
            case "s3-storage" -> bucketImageService;
            default -> localImageService;
        };
    }
}
