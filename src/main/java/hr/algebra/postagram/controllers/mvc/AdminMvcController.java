package hr.algebra.postagram.controllers.mvc;

import hr.algebra.postagram.models.Event;
import hr.algebra.postagram.models.Package;
import hr.algebra.postagram.models.Post;
import hr.algebra.postagram.models.User;
import hr.algebra.postagram.models.dtos.AdminUserEditDto;
import hr.algebra.postagram.services.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("mvc/admin")
public class AdminMvcController {
    private final EventService eventService;
    private final PackageService packageService;
    private final UserService userService;
    private final PostService postService;
    private final Mapper mapper;
    private static final String MODEL_ATTRIBUTE_LOGS = "logs";
    private static final String MODEL_ATTRIBUTE_USER_EDIT = "userEdit";
    private static final String MODEL_ATTRIBUTE_PAGES = "totalPages";
    private static final String MODEL_ATTRIBUTE_CURRENT_PAGE = "currentPage";
    private static final String MODEL_ATTRIBUTE_USERNAME = "username";
    private static final String MODEL_ATTRIBUTE_USERS = "users";
    private static final String MODEL_ATTRIBUTE_POSTS = "users";
    private static final String MODEL_ATTRIBUTE_SIZE = "size";
    private static final String LOGS_PAGE = "logs";
    private static final String USERS_PAGE = "users";
    private static final String USER_DETAILS_PAGE = "userDetails";

    public AdminMvcController(EventService eventService, PackageService packageService, UserService userService, PostService postService, Mapper mapper) {
        this.eventService = eventService;
        this.packageService = packageService;
        this.userService = userService;
        this.postService = postService;
        this.mapper = mapper;
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

            model.addAttribute(MODEL_ATTRIBUTE_PAGES, paged.getTotalPages());
            model.addAttribute(MODEL_ATTRIBUTE_CURRENT_PAGE, page);
            model.addAttribute(MODEL_ATTRIBUTE_SIZE, size);
            model.addAttribute(MODEL_ATTRIBUTE_POSTS, paged.getContent().stream().map(mapper::postToDto));
        }

        return USER_DETAILS_PAGE;
    }

    @PutMapping("users/{id}")
    public String updateUser(Model model, @PathVariable Long id, @ModelAttribute(MODEL_ATTRIBUTE_USER_EDIT) @Valid AdminUserEditDto editDto) {
        Optional<User> userById = userService.findById(id);
        Optional<Package> packageById = packageService.findById(editDto.getPackageId());
        if(userById.isPresent() && packageById.isPresent()){
            User user = userById.get();
            user.setUsername(editDto.getUsername());
            user.setUserPackage(packageById.get());
            userService.save(user);
        }
        return "redirect:/mvc/admin/users/"+id.toString();
    }
}
