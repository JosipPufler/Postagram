package hr.algebra.postagram.services;

import hr.algebra.postagram.models.ImageData;
import hr.algebra.postagram.models.Post;
import hr.algebra.postagram.models.StorageTypeEnum;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ImageLoader {
    private final LocalImageService localImageService;
    private final BucketImageService bucketImageService;

    public ImageLoader(LocalImageService localImageService, BucketImageService bucketImageService) {
        this.localImageService = localImageService;
        this.bucketImageService = bucketImageService;
    }

    public Optional<ImageData> loadImage(Post post){
        switch (StorageTypeEnum.valueOf(post.getStorageType())) {
            case S3 -> {
                return bucketImageService.load(post.getImageId());
            }
            case DB -> {
                return localImageService.load(post.getImageId());
            }
            default -> {
                return Optional.empty();
            }
        }
    }

    public void deleteImage(Post post){
        switch (StorageTypeEnum.valueOf(post.getStorageType())) {
            case S3 -> bucketImageService.delete(post.getImageId());
            case DB -> localImageService.delete(post.getImageId());
        }
    }
}
