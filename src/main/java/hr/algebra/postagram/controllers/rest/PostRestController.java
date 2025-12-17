package hr.algebra.postagram.controllers.rest;

import hr.algebra.postagram.models.ImageData;
import hr.algebra.postagram.models.Post;
import hr.algebra.postagram.services.ImageService;
import hr.algebra.postagram.services.PostService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/rest")
public class PostRestController {
    private final PostService postService;
    private final ImageService imageService;

    public PostRestController(PostService postService, ImageService imageService) {
        this.postService = postService;
        this.imageService = imageService;
    }

    @GetMapping("/public/post/{id}/image")
    public ResponseEntity<byte[]> loadPostImage(@PathVariable long id) {
        Optional<Post> byId = postService.findById(id);

        if (byId.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Post post = byId.get();
        ImageData imageData = imageService.load(post.getImageId());
        if (imageData == null) {
            return ResponseEntity.notFound().build();
        }

        MediaType type = MediaType.parseMediaType(imageData.getContentType());

        return ResponseEntity.ok()
                .contentType(type)
                .body(imageData.getData());
    }
}
