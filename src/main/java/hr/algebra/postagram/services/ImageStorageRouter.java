package hr.algebra.postagram.services;

import hr.algebra.postagram.models.ImageData;
import hr.algebra.postagram.models.Post;
import hr.algebra.postagram.models.StorageTypeEnum;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
public class ImageStorageRouter {
    private final Map<StorageTypeEnum, Function<String, Optional<ImageData>>> loaders = new EnumMap<>(StorageTypeEnum.class);
    private final Map<StorageTypeEnum, Consumer<String>> deleters = new EnumMap<>(StorageTypeEnum.class);
    private final Map<StorageTypeEnum, BiFunction<byte[], String, String>> writers = new EnumMap<>(StorageTypeEnum.class);

    @Getter
    private final StorageTypeEnum storageType;

    public ImageStorageRouter(
            @Value("${image.storage-type.write}") String storageType,
            LocalImageService localImageService,
            BucketImageService bucketImageService) {
        this.storageType = StorageTypeEnum.valueOfOrElse(storageType);

        Set.of(localImageService, bucketImageService)
                .forEach(imageService -> {
                    loaders.put(imageService.getStorageType(), imageService::load);
                    deleters.put(imageService.getStorageType(), imageService::delete);
                    writers.put(imageService.getStorageType(), imageService::store);
                });
    }

    public Optional<ImageData> loadImage(Post post) {
        return Optional.ofNullable(
                loaders.get(StorageTypeEnum.valueOf(post.getStorageType()))
        ).flatMap(loader -> loader.apply(post.getImageId()));
    }

    public void deleteImage(Post post){
        Optional.ofNullable(deleters.get(StorageTypeEnum.valueOf(post.getStorageType())))
                .ifPresent(deleter -> deleter.accept(post.getImageId()));
    }

    public String storeImage(byte[] imageData, String contentType) {
        return writers.get(storageType).apply(imageData, contentType);
    }

    public String storeImage(byte[] imageData, String contentType, StorageTypeEnum storageType) {
        return writers.get(storageType).apply(imageData, contentType);
    }
}
