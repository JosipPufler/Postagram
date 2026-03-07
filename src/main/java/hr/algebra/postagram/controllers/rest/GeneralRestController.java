package hr.algebra.postagram.controllers.rest;

import hr.algebra.postagram.models.*;
import hr.algebra.postagram.models.dtos.ClientEvent;
import hr.algebra.postagram.services.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/rest")
public class GeneralRestController {
    private final PostService postService;
    private final ImageLoader imageLoader;
    private final EventService eventService;
    private final EventTypeService eventTypeService;
    private final UserService userService;

    public GeneralRestController(PostService postService, ImageLoader imageLoader, EventService eventService, EventTypeService eventTypeService, UserService userService) {
        this.postService = postService;
        this.imageLoader = imageLoader;
        this.eventService = eventService;
        this.eventTypeService = eventTypeService;
        this.userService = userService;
    }

    @GetMapping("/public/post/{id}/image")
    public ResponseEntity<byte[]> loadPostImage(@PathVariable long id) {
        Optional<Post> byId = postService.findById(id);

        if (byId.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Post post = byId.get();

        Optional<ImageData> imageData = imageLoader.loadImage(post);
        if (imageData.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        MediaType type = MediaType.parseMediaType(imageData.get().getContentType());

        return ResponseEntity.ok()
                .contentType(type)
                .body(imageData.get().getData());
    }

    @PostMapping("/public/event")
    public ResponseEntity<Void> eventPost(@RequestBody ClientEvent event) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = ((CustomUserDetails) auth.getPrincipal()).getId();
        Optional<User> userById = userService.findById(userId);
        EventType eventType = eventTypeService.findByEnum(EventTypeEnum.valueOf(event.getEventType()));
        if (userById.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        eventService.save(Event.builder().eventType(eventType).user(userById.get().getId()).description(event.getMessage()).time(LocalDateTime.now()).build());

        return ResponseEntity.ok().build();
    }
}
