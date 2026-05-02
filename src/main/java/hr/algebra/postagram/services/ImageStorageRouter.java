package hr.algebra.postagram.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ImageStorageRouter {
    private final Map<String, ImageService> writers;
    private final String storageType;

    public ImageStorageRouter(
            @Value("${image.storage-type.write}") String storageType,
            LocalImageService localImageService,
            BucketImageService bucketImageService
    ) {
        this.storageType = storageType;

        this.writers = Map.of(
                "db", localImageService,
                "s3", bucketImageService
        );
    }

    public ImageService getWriter() {
        return writers.getOrDefault(storageType.toLowerCase(), writers.get("db-storage"));
    }
}
