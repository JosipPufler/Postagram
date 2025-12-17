package hr.algebra.postagram.controllers.mvc;

import hr.algebra.postagram.models.*;
import hr.algebra.postagram.models.Package;
import hr.algebra.postagram.models.dtos.AdminUserEditDto;
import hr.algebra.postagram.models.dtos.PackageDto;
import hr.algebra.postagram.models.dtos.PostForm;
import hr.algebra.postagram.models.events.AdminPostUpdate;
import hr.algebra.postagram.models.events.AdminProfileUpdate;
import hr.algebra.postagram.models.events.PostEvent;
import hr.algebra.postagram.services.*;
import jakarta.validation.Valid;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("mvc/admin")
public class AdminMvcController {
    private final EventService eventService;
    private final PackageService packageService;
    private final UserService userService;
    private final PostService postService;
    private final HashtagService hashtagService;
    private final ImageService imageService;
    private final ApplicationEventPublisher publisher;
    private final Mapper mapper;
    private static final String MODEL_ATTRIBUTE_LOGS = "logs";
    private static final String MODEL_ATTRIBUTE_USER_EDIT = "userEdit";
    private static final String MODEL_ATTRIBUTE_USERNAME = "username";
    private static final String MODEL_ATTRIBUTE_USERS = "users";
    private static final String MODEL_ATTRIBUTE_POSTS = "posts";
    private static final String MODEL_ATTRIBUTE_SIZE = "size";
    private static final String MODEL_ATTRIBUTE_HASHTAGS = "hashtags";
    private static final String MODEL_ATTRIBUTE_PAGES = "totalPages";
    private static final String MODEL_ATTRIBUTE_POST_FORM = "postForm";
    private static final String MODEL_ATTRIBUTE_CURRENT_PAGE = "currentPage";
    private static final String LOGS_PAGE = "logs";
    private static final String USERS_PAGE = "users";
    private static final String USER_DETAILS_PAGE = "userDetails";
    private static final String MODEL_ATTRIBUTE_PACKAGES = "packages";
    private static final String MODEL_ATTRIBUTE_USER_DTO = "userDto";
    private static final String MODEL_ATTRIBUTE_PACKAGE_USAGE = "packageUsage";

    public AdminMvcController(EventService eventService, PackageService packageService, UserService userService, PostService postService, HashtagService hashtagService, ImageService imageService, ApplicationEventPublisher publisher, Mapper mapper) {
        this.eventService = eventService;
        this.packageService = packageService;
        this.userService = userService;
        this.postService = postService;
        this.hashtagService = hashtagService;
        this.imageService = imageService;
        this.publisher = publisher;
        this.mapper = mapper;
    }

    @ModelAttribute(MODEL_ATTRIBUTE_POST_FORM)
    public PostForm postForm() {
        return new PostForm();
    }

    @ModelAttribute(MODEL_ATTRIBUTE_HASHTAGS)
    public List<String> hashtags() {
        return hashtagService.findAll().stream().map(Hashtag::toString).toList();
    }

    @GetMapping("logs")
    public String getLogs(Model model, @RequestParam(defaultValue = "") String username, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size){
        Page<Event> paged = eventService.findPaged(page, size, username);

        model.addAttribute(MODEL_ATTRIBUTE_PAGES, paged.getTotalPages());
        model.addAttribute(MODEL_ATTRIBUTE_CURRENT_PAGE, page);
        model.addAttribute(MODEL_ATTRIBUTE_LOGS, paged.getContent().stream().map(mapper::eventToDto));
        model.addAttribute(MODEL_ATTRIBUTE_SIZE, size);
        return LOGS_PAGE;
    }

    @GetMapping("users")
    public String getUsers(Model model, @RequestParam(defaultValue = "") String username, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size){
        Page<User> paged = userService.findByUsernamePaged(username, page, size);

        model.addAttribute(MODEL_ATTRIBUTE_USERNAME, username);
        model.addAttribute(MODEL_ATTRIBUTE_PAGES, paged.getTotalPages());
        model.addAttribute(MODEL_ATTRIBUTE_CURRENT_PAGE, page);
        model.addAttribute(MODEL_ATTRIBUTE_USERS, paged.getContent().stream().map(mapper::userToDto));
        model.addAttribute(MODEL_ATTRIBUTE_SIZE, size);

        return USERS_PAGE;
    }

    @GetMapping("users/{id}")
    public String getUsers(Model model, @PathVariable Long id, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size){
        Optional<User> byId = userService.findById(id);

        if(byId.isPresent()){
            Page<Post> paged = postService.findByUserPaged(byId.get(), page, size);
            List<Package> packages = packageService.findAll();

            model.addAttribute(MODEL_ATTRIBUTE_PAGES, paged.getTotalPages());
            model.addAttribute(MODEL_ATTRIBUTE_CURRENT_PAGE, page);
            model.addAttribute(MODEL_ATTRIBUTE_SIZE, size);
            model.addAttribute(MODEL_ATTRIBUTE_POSTS, paged.getContent().stream().map(mapper::postToDto).toList());
            model.addAttribute(MODEL_ATTRIBUTE_PACKAGES, packages.stream().map(mapper::packageToDto).sorted(Comparator.comparing(PackageDto::getId)).toList());
            model.addAttribute(MODEL_ATTRIBUTE_USER_DTO, mapper.userToDto(byId.get()));
            model.addAttribute(MODEL_ATTRIBUTE_PACKAGE_USAGE, mapper.userToPackageUsageInfo(byId.get()));
        }

        return USER_DETAILS_PAGE;
    }

    @PostMapping("users/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute(MODEL_ATTRIBUTE_USER_EDIT) @Valid AdminUserEditDto editDto) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = ((CustomUserDetails) auth.getPrincipal()).getId();
        Optional<User> adminById = userService.findById(userId);

        Optional<User> userById = userService.findById(id);
        Optional<Package> packageById = packageService.findById(editDto.getPackageId());
        if(userById.isPresent() && packageById.isPresent() && adminById.isPresent()){
            User user = userById.get();
            user.setUsername(editDto.getUsername());
            user.setUserPackage(packageById.get());
            userService.save(user);

            publisher.publishEvent(new AdminProfileUpdate(adminById.get(), user));
        }
        return "redirect:/mvc/admin/users/"+id.toString();
    }

    @PostMapping("edit")
    public String updateUser(@Valid @ModelAttribute(MODEL_ATTRIBUTE_POST_FORM)PostForm postForm) throws IOException {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = ((CustomUserDetails) auth.getPrincipal()).getId();
        Optional<User> adminById = userService.findById(userId);

        Optional<Post> byId = postService.findById(postForm.getId());
        if(byId.isPresent() && adminById.isPresent()){
            String store = imageService.store(postForm.getImage().getBytes(), postForm.getImage().getContentType());
            Post post = byId.get();
            imageService.delete(post.getImageId());
            post.setImageId(store);
            post.updateImageData(postForm.getImage());
            post.setDescription(postForm.getDescription());
            post.setHashtags(postForm.getHashtags().stream().map(x -> hashtagService.findByNameOrCreate(x, post.getUser())).collect(Collectors.toSet()));
            postService.save(post);

            publisher.publishEvent(new AdminPostUpdate(adminById.get(), byId.get()));

            return "redirect:/mvc/admin/users/"+post.getUser().getId().toString();
        }
        return "redirect:/mvc/admin/users";
    }
}
