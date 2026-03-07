package hr.algebra.postagram.services;

import hr.algebra.postagram.models.Image;
import hr.algebra.postagram.models.ImageData;
import hr.algebra.postagram.repositories.ImageRepo;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LocalImageService implements ImageService {

    private final ImageRepo repo;

    public LocalImageService(ImageRepo repo) {
        this.repo = repo;
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
    public Optional<ImageData> load(String key) {
        Optional<Image> byId = repo.findById(key);
        return byId.map(image -> new ImageData(image.getImage(), image.getContentType()));
    }

    @Override
    public void delete(String key) {
        repo.deleteById(key);
    }
}
