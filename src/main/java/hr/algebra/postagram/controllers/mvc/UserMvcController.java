package hr.algebra.postagram.controllers.mvc;

import hr.algebra.postagram.models.CustomUserDetails;
import hr.algebra.postagram.models.Package;
import hr.algebra.postagram.models.User;
import hr.algebra.postagram.models.dtos.PackageDto;
import hr.algebra.postagram.models.dtos.PackageSelectionDto;
import hr.algebra.postagram.models.dtos.RegistrationForm;
import hr.algebra.postagram.models.dtos.UserDto;
import hr.algebra.postagram.models.events.UserProfileUpdate;
import hr.algebra.postagram.services.Mapper;
import hr.algebra.postagram.services.PackageService;
import hr.algebra.postagram.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/auth")
public class UserMvcController {
    private final UserService userService;
    private final PackageService packageService;
    private final Mapper mapper;
    private final ApplicationEventPublisher publisher;
    private static final String MODEL_ATTRIBUTE_PACKAGES = "packages";
    private static final String MODEL_ATTRIBUTE_USER_DTO = "userDto";
    private static final String MODEL_ATTRIBUTE_REGISTRATION_FORM = "registrationForm";
    private static final String MODEL_ATTRIBUTE_PACKAGE_USAGE = "packageUsage";
    private static final String REGISTER_PAGE = "register";
    private static final String PROFILE_PAGE = "profile";
    private static final String LOGIN_PAGE = "login";

    public UserMvcController(UserService userService, PackageService packageService, Mapper mapper, ApplicationEventPublisher publisher) {
        this.userService = userService;
        this.packageService = packageService;
        this.mapper = mapper;
        this.publisher = publisher;
    }

    @GetMapping("/login")
    public String showLoginPage(
            @RequestParam(value="error", required=false) String error,
            @RequestParam(value="logout", required=false) String logout,
            Model model
    ) {
        if (error != null) {
            model.addAttribute("loginError", true);
        }
        if (logout != null) {
            model.addAttribute("logoutSuccess", true);
        }
        return LOGIN_PAGE;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        List<Package> packages = packageService.findAll();
        model.addAttribute(MODEL_ATTRIBUTE_PACKAGES, packages.stream().map(mapper::packageToDto).toList());
        RegistrationForm form = new RegistrationForm();
        form.setPackageId(packages.getFirst().getId());
        model.addAttribute(MODEL_ATTRIBUTE_REGISTRATION_FORM, form);
        return REGISTER_PAGE;
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("registrationForm") @Valid RegistrationForm registrationForm, BindingResult result) {
        if (!registrationForm.getPassword().equals(registrationForm.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.userDto", "Passwords do not match.");
        }

        if (userService.usernameExists(registrationForm.getUsername())) {
            result.rejectValue("username", "error.userDto", "Username already exists.");
        }

        if (result.hasErrors()) {
            return REGISTER_PAGE;
        }

        userService.save(mapper.registerFormToUser(registrationForm));
        return "redirect:/auth/login?registered";
    }

    @GetMapping("/profile")
    public String showProfile(Model model) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = ((CustomUserDetails) auth.getPrincipal()).getId();
        Optional<User> byId = userService.findById(userId);

        if (byId.isEmpty()) {
            return "redirect:/auth/login";
        }

        List<Package> packages = packageService.findAll();
        model.addAttribute(MODEL_ATTRIBUTE_PACKAGES, packages.stream().map(mapper::packageToDto).sorted(Comparator.comparing(PackageDto::getId)).toList());
        model.addAttribute(MODEL_ATTRIBUTE_USER_DTO, mapper.userToDto(byId.get()));
        model.addAttribute(MODEL_ATTRIBUTE_PACKAGE_USAGE, mapper.userToPackageUsageInfo(byId.get()));

        return PROFILE_PAGE;
    }

    @PostMapping("/profile/edit")
    public String updateProfile(@ModelAttribute(MODEL_ATTRIBUTE_USER_DTO) @Valid UserDto userDto, BindingResult result) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = ((CustomUserDetails) auth.getPrincipal()).getId();
        Optional<User> byId = userService.findById(userId);

        if (byId.isEmpty()) {
            return "redirect:/auth/login";
        }

        if (!Objects.equals(byId.get().getUsername(), userDto.getUsername()) && userService.usernameExists(userDto.getUsername())) {
            result.rejectValue("username", "error.userDto", "Username already exists.");
        }

        if (!Objects.equals(byId.get().getEmail(), userDto.getEmail()) && userService.emailExists(userDto.getEmail())) {
            result.rejectValue("email", "error.userDto", "Email already exists.");
        }

        if (result.hasErrors()) {
            return "redirect:/auth/profile";
        }

        userDto.setId(userId);

        userService.saveFromDto(userDto);
        publisher.publishEvent(new UserProfileUpdate(byId.get()));
        return "redirect:/auth/profile";
    }

    @GetMapping("/register/oauth2")
    public String showOauth2PackageSelection(Model model) {
        model.addAttribute(MODEL_ATTRIBUTE_PACKAGES, packageService.findAll().stream().map(mapper::packageToDto).toList());
        model.addAttribute("packageSelection", new PackageSelectionDto());
        return "register-oauth2";
    }

    @PostMapping("/register/oauth2")
    public String completeOauth2Registration(@ModelAttribute PackageSelectionDto packageSelection, HttpServletRequest request) {
        String username = (String) request.getSession().getAttribute("oauthUsername");
        userService.registerOauthUser(packageSelection.getPackageId(), username);
        request.getSession().removeAttribute("oauthEmail");
        return "redirect:/mvc/public/post/home";
    }
}
