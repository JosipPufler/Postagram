package hr.algebra.postagram.controllers.mvc;

import hr.algebra.postagram.models.CustomUserDetails;
import hr.algebra.postagram.models.Hashtag;
import hr.algebra.postagram.models.Post;
import hr.algebra.postagram.models.User;
import hr.algebra.postagram.models.dtos.PostDto;
import hr.algebra.postagram.models.dtos.PostForm;
import hr.algebra.postagram.models.dtos.PostSearchForm;
import hr.algebra.postagram.models.events.AdminPostUpdate;
import hr.algebra.postagram.models.events.PostEvent;
import hr.algebra.postagram.models.events.UserPostUpdate;
import hr.algebra.postagram.services.*;
import jakarta.validation.Valid;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("mvc/public/post")
public class PostMvcController {
    private final PostService postService;
    private final HashtagService hashtagService;
    private final UserService userService;
    private final ApplicationEventPublisher publisher;
    private final ImageService imageService;
    private final Mapper mapper;
    private static final String MODEL_ATTRIBUTE_POST_FORM = "postForm";
    private static final String MODEL_ATTRIBUTE_SEARCH_FORM = "searchForm";
    private static final String MODEL_ATTRIBUTE_POSTS = "posts";
    private static final String MODEL_ATTRIBUTE_HASHTAGS = "hashtags";
    private static final String HOME_PAGE = "home";
    private static final String MY_POSTS_PAGE = "myPosts";
    private static final String REDIRECT_TO_HOME_PAGE = "redirect:/mvc/public/post/home";
    private static final String REDIRECT_TO_LOGIN = "redirect:/auth/login";
    private static final String SEARCH_PAGE = "postFilter";
    private static final String MODEL_ATTRIBUTE_SIZE = "size";
    private static final String MODEL_ATTRIBUTE_PAGES = "totalPages";
    private static final String MODEL_ATTRIBUTE_CURRENT_PAGE = "currentPage";


    public PostMvcController(PostService postService, HashtagService hashtagService, UserService userService, ApplicationEventPublisher publisher, ImageService imageService, Mapper mapper) {
        this.postService = postService;
        this.hashtagService = hashtagService;
        this.userService = userService;
        this.publisher = publisher;
        this.imageService = imageService;
        this.mapper = mapper;
    }

    @ModelAttribute(MODEL_ATTRIBUTE_POST_FORM)
    public PostForm postForm() {
        return new PostForm();
    }

    @ModelAttribute(MODEL_ATTRIBUTE_SEARCH_FORM)
    public PostSearchForm searchForm() {
        return new PostSearchForm();
    }

    @ModelAttribute(MODEL_ATTRIBUTE_HASHTAGS)
    public List<String> hashtags() {
        return hashtagService.findAll().stream().map(Hashtag::toString).toList();
    }

    @GetMapping("/home")
    public String getHome(Model model, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        List<PostDto> randomPosts = postService.getLatestPaged(page, size).stream().map(mapper::postToDto).toList();
        model.addAttribute(MODEL_ATTRIBUTE_POSTS, randomPosts);
        return HOME_PAGE;
    }

    @GetMapping("/my")
    public String getMyPosts(Model model, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = ((CustomUserDetails) auth.getPrincipal()).getId();
        Optional<User> userById = userService.findById(userId);
        if (userById.isEmpty()) {
            return REDIRECT_TO_LOGIN;
        }
        Page<Post> paged = postService.findByUserPaged(userById.get(), page, size);

        model.addAttribute(MODEL_ATTRIBUTE_PAGES, paged.getTotalPages());
        model.addAttribute(MODEL_ATTRIBUTE_CURRENT_PAGE, page);
        model.addAttribute(MODEL_ATTRIBUTE_SIZE, size);
        model.addAttribute(MODEL_ATTRIBUTE_POSTS, paged.getContent().stream().map(mapper::postToDto).toList());
        return MY_POSTS_PAGE;
    }

    @PostMapping("/my/edit")
    public String updateUser(@Valid @ModelAttribute(MODEL_ATTRIBUTE_POST_FORM)PostForm postForm) throws IOException {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = ((CustomUserDetails) auth.getPrincipal()).getId();
        Optional<User> userById = userService.findById(userId);

        Optional<Post> byId = postService.findById(postForm.getId());
        if(byId.isPresent() && userById.isPresent() && Objects.equals(byId.get().getUser().getId(), userById.get().getId())){
            String store = imageService.store(postForm.getImage().getBytes(), postForm.getImage().getContentType());
            Post post = byId.get();
            imageService.delete(post.getImageId());
            post.setImageId(store);
            post.updateImageData(postForm.getImage());
            post.setDescription(postForm.getDescription());
            post.setHashtags(postForm.getHashtags().stream().map(x -> hashtagService.findByNameOrCreate(x, post.getUser())).collect(Collectors.toSet()));
            postService.save(post);

            publisher.publishEvent(new UserPostUpdate(byId.get()));

            return "redirect:/mvc/public/post/my";
        }
        return REDIRECT_TO_LOGIN;
    }

    @GetMapping("/search")
    public String getSearchForm() {
        return SEARCH_PAGE;
    }

    @GetMapping("/search/submit")
    public String getHome(@Valid @ModelAttribute(MODEL_ATTRIBUTE_SEARCH_FORM) PostSearchForm searchForm, Model model) {
        List<PostDto> searchPosts = postService.filterPosts(searchForm).stream().map(mapper::postToDto).toList();
        model.addAttribute(MODEL_ATTRIBUTE_POSTS, searchPosts);
        return HOME_PAGE;
    }

    @PostMapping("/create")
    public String createPost(@Valid @ModelAttribute(MODEL_ATTRIBUTE_POST_FORM) PostForm postForm, Model model, BindingResult result, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = ((CustomUserDetails) auth.getPrincipal()).getId();
        Optional<User> userById = userService.findById(userId);
        if (result.hasErrors() || userById.isEmpty()) {
            model.addAttribute(MODEL_ATTRIBUTE_HASHTAGS, hashtagService.findAll());
            return REDIRECT_TO_HOME_PAGE;
        }
        User user = userById.get();

        if (!user.canPublishPost(postForm)){
            model.addAttribute(MODEL_ATTRIBUTE_HASHTAGS, hashtagService.findAll());
            model.addAttribute(MODEL_ATTRIBUTE_POSTS, postService.getLatestPaged(page, size).stream().map(mapper::postToDto).toList());
            result.rejectValue(MODEL_ATTRIBUTE_POST_FORM, "error", "You are exceeding your package limits");
            return REDIRECT_TO_HOME_PAGE;
        }

        Optional<Post> post = mapper.formToPost(postForm, userById.get());
        model.addAttribute(MODEL_ATTRIBUTE_HASHTAGS, hashtagService.findAll());
        if (post.isEmpty()){
            result.rejectValue(MODEL_ATTRIBUTE_HASHTAGS, "error", "There was an error when adding your hashtags");
            model.addAttribute(MODEL_ATTRIBUTE_POSTS, postService.getLatestPaged(page, size).stream().map(mapper::postToDto).toList());
            return REDIRECT_TO_HOME_PAGE;
        }

        model.addAttribute(MODEL_ATTRIBUTE_POSTS, postService.getLatestPaged(page, size).stream().map(mapper::postToDto).toList());
        postService.save(post.get());
        userService.save(user);
        publisher.publishEvent(new PostEvent(post.get()));

        return REDIRECT_TO_HOME_PAGE;
    }
}
