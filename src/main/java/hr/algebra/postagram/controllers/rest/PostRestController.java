package hr.algebra.postagram.controllers.rest;

import hr.algebra.postagram.models.Post;
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

    public PostRestController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/public/post/{id}/image")
    public ResponseEntity<byte[]> loadPostImage(@PathVariable long id) {
        Optional<Post> byId = postService.findById(id);

        if (byId.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Post post = byId.get();
        if (post.getImage() == null) {
            return ResponseEntity.notFound().build();
        }

        MediaType type = MediaType.parseMediaType(post.getImageContentType());

        return ResponseEntity.ok()
                .contentType(type)
                .body(post.getImage());
    }
}
