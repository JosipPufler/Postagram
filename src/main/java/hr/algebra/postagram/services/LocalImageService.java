package hr.algebra.postagram.services;

import hr.algebra.postagram.models.Image;
import hr.algebra.postagram.models.ImageData;
import hr.algebra.postagram.repositories.ImageRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Profile("db-storage")
public class LocalImageService implements ImageService {

    private final ImageRepository repo;
    private final Mapper mapper;

    public LocalImageService(ImageRepository repo, Mapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public String store(byte[] data, String contentType) {
        Image img = new Image();
        img.setImage(data);
        img.setContentType(contentType);
        repo.save(img);
        return img.getId().toString();
    }

    @Override
    public ImageData load(String key) {
        return mapper.imageToImageData(repo.findById(UUID.fromString(key)).orElseThrow());
    }

    @Override
    public void delete(String key) {
        repo.deleteById(UUID.fromString(key));
    }
}
