package hr.algebra.postagram.services;

import hr.algebra.postagram.models.ImageData;

import java.util.Optional;

public interface ImageService {
    String store(byte[] data, String contentType);

    Optional<ImageData> load(String key);

    void delete(String key);
}
