package hr.algebra.postagram.controllers.mvc;

import hr.algebra.postagram.models.CustomUserDetails;
import hr.algebra.postagram.models.Hashtag;
import hr.algebra.postagram.models.Post;
import hr.algebra.postagram.models.User;
import hr.algebra.postagram.models.dtos.PostDto;
import hr.algebra.postagram.models.dtos.PostForm;
import hr.algebra.postagram.models.dtos.PostSearchForm;
import hr.algebra.postagram.models.events.PostEvent;
import hr.algebra.postagram.services.*;
import jakarta.validation.Valid;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("mvc/public/post")
public class PostMvcController {
    private final PostService postService;
    private final HashtagService hashtagService;
    private final UserService userService;
    private final ApplicationEventPublisher publisher;
    private final Mapper mapper;
    private static final Integer DEFAULT_LOAD_AMOUNT = 10;
    private static final String MODEL_ATTRIBUTE_POST_FORM = "postForm";
    private static final String MODEL_ATTRIBUTE_SEARCH_FORM = "searchForm";
    private static final String MODEL_ATTRIBUTE_POSTS = "posts";
    private static final String MODEL_ATTRIBUTE_HASHTAGS = "hashtags";
    private static final String HOME_PAGE = "home";
    private static final String REDIRECT_TO_HOME_PAGE = "redirect:/mvc/public/post/home";
    private static final String SEARCH_PAGE = "postFilter";

    public PostMvcController(PostService postService, HashtagService hashtagService, UserService userService, ApplicationEventPublisher publisher, Mapper mapper) {
        this.postService = postService;
        this.hashtagService = hashtagService;
        this.userService = userService;
        this.publisher = publisher;
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
    public String getHome(Model model) {
        List<PostDto> randomPosts = postService.getLatest(DEFAULT_LOAD_AMOUNT).stream().map(mapper::postToDto).toList();
        model.addAttribute(MODEL_ATTRIBUTE_POSTS, randomPosts);
        return HOME_PAGE;
    }

    @GetMapping("/search")
    public String getSearchForm() {
        return SEARCH_PAGE;
    }

    @GetMapping("/search/submit")
    public String getHome(@Valid @ModelAttribute(MODEL_ATTRIBUTE_SEARCH_FORM) PostSearchForm searchForm, Model model) {
        List<PostDto> searchPosts = postService.getPostsBySearchForm(searchForm).stream().map(mapper::postToDto).toList();
        model.addAttribute(MODEL_ATTRIBUTE_POSTS, searchPosts);
        return HOME_PAGE;
    }

    @PostMapping("/create")
    public String createPost(@Valid @ModelAttribute(MODEL_ATTRIBUTE_POST_FORM) PostForm postForm, Model model, BindingResult result) {
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
            model.addAttribute(MODEL_ATTRIBUTE_POSTS, postService.getLatest(DEFAULT_LOAD_AMOUNT).stream().map(mapper::postToDto).toList());
            result.rejectValue(MODEL_ATTRIBUTE_POST_FORM, "error", "You are exceeding your package limits");
            return REDIRECT_TO_HOME_PAGE;
        }

        Optional<Post> post = mapper.formToPost(postForm, userById.get());
        model.addAttribute(MODEL_ATTRIBUTE_HASHTAGS, hashtagService.findAll());
        if (post.isEmpty()){
            result.rejectValue(MODEL_ATTRIBUTE_HASHTAGS, "error", "There was an error when adding your hashtags");
            model.addAttribute(MODEL_ATTRIBUTE_POSTS, postService.getLatest(DEFAULT_LOAD_AMOUNT).stream().map(mapper::postToDto).toList());
            return REDIRECT_TO_HOME_PAGE;
        }

        model.addAttribute(MODEL_ATTRIBUTE_POSTS, postService.getLatest(DEFAULT_LOAD_AMOUNT).stream().map(mapper::postToDto).toList());
        postService.save(post.get());
        user.publishPost(postForm);
        userService.save(user);
        publisher.publishEvent(new PostEvent(post.get()));

        return REDIRECT_TO_HOME_PAGE;
    }
}
