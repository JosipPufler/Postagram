package hr.algebra.postagram.services;

import hr.algebra.postagram.models.ImageData;

public interface ImageService {
    String store(byte[] data, String contentType);

    ImageData load(String key);

    void delete(String key);
}
