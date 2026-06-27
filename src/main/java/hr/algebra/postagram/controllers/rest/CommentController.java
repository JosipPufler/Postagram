package hr.algebra.postagram.controllers.rest;

import hr.algebra.postagram.models.Comment;
import hr.algebra.postagram.models.CustomUserDetails;
import hr.algebra.postagram.models.Post;
import hr.algebra.postagram.models.User;
import hr.algebra.postagram.models.dtos.CommentDto;
import hr.algebra.postagram.models.dtos.CommentRequest;
import hr.algebra.postagram.services.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/rest/public/comment")
public class CommentController {
    private final CommentService commentService;
    private final PostService postService;
    private final UserService userService;
    private final Mapper mapper;

    public CommentController(CommentService commentService, PostService postService, UserService userService, Mapper mapper) {
        this.commentService = commentService;
        this.postService = postService;
        this.userService = userService;
        this.mapper = mapper;
    }

    @PostMapping("/{id}")
    public ResponseEntity<Void> addComment(@RequestBody CommentRequest comment, @PathVariable Long id) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = ((CustomUserDetails) auth.getPrincipal()).getId();
        Optional<User> userById = userService.findById(userId);

        if (userById.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Post> postById = postService.findById(id);

        if (postById.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Comment newComment = Comment.builder()
                .user(userById.get())
                .post(postById.get())
                .content(comment.content())
                .postedAt(LocalDateTime.now())
                .build();

        commentService.save(newComment);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<CommentDto>> getCommentsForPostId(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.getCommentsByPostId(id).stream().map(mapper::commentToDto).toList());
    }
}
