package hr.algebra.postagram.services;

import hr.algebra.postagram.configs.PasswordEncoderProvider;
import hr.algebra.postagram.models.*;
import hr.algebra.postagram.models.Package;
import hr.algebra.postagram.models.dtos.*;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class Mapper {
    private final RoleService roleService;
    private final HashtagService hashtagService;
    private final PackageService packageService;
    private final ImageService imageService;

    public Mapper(RoleService roleService, HashtagService hashtagService, PackageService packageService, ImageService imageService){
        this.roleService = roleService;
        this.hashtagService = hashtagService;
        this.packageService = packageService;
        this.imageService = imageService;
    }

    public UserDto userToDto(User user) {
        return  UserDto.builder()
                .id(user.getId())
                .packageId(user.getUserPackage().getId())
                .packageName(user.getUserPackage().getName())
                .username(user.getUsername())
                .email(user.getEmail() == null ? "None" : user.getEmail() )
                .build();
    }

    public User registerFormToUser(RegistrationForm registrationForm) {
        return User.builder()
                .email(registrationForm.getEmail())
                .password(PasswordEncoderProvider.getStaticPasswordEncoder().encode(registrationForm.getPassword()))
                .username(registrationForm.getUsername())
                .roles(List.of(roleService.findByEnum(RoleEnum.USER)))
                .userPackage(packageService.findById(registrationForm.getPackageId()).orElseThrow())
                .uploadCount(0)
                .uploadedAmount(0L)
                .active(true)
                .build();
    }

    public Optional<Post> formToPost(PostForm postForm, User user){
        try {
            BufferedImage buffered = ImageIO.read(postForm.getImage().getInputStream());
            Post post = Post.builder()
                    .id(postForm.getId())
                    .hashtags(postForm.getHashtags().stream().map(x -> hashtagService.findByNameOrCreate(x, user)).collect(Collectors.toCollection(HashSet::new)))
                    .imageId(imageService.store(postForm.getImage().getBytes(), postForm.getImage().getContentType()))
                    .imageWidth(buffered.getWidth())
                    .imageHeight(buffered.getHeight())
                    .aspectRatio((double)buffered.getWidth()/buffered.getHeight())
                    .description(postForm.getDescription())
                    .postedAt(LocalDateTime.now())
                    .user(user)
                    .build();
            return Optional.of(post);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public PackageDto packageToDto(Package pack) {
        return new PackageDto(pack);
    }

    public Optional<Package> dtoToPackage(PackageDto packageDto) {
        return packageService.findById(packageDto.getId());
    }

    public PostDto postToDto(Post post) {
        return new PostDto(post);
    }

    public EventDto eventToDto(Event event) {
        return EventDto.builder()
                .id(event.getId())
                .eventType(event.getEventType().getName())
                .createdAt(event.getTime())
                .userId(event.getUser().getId())
                .description(event.getDescription())
                .username(event.getUser().getUsername())
                .build();
    }

    public PackageUsageInfoDto userToPackageUsageInfo(User user) {
        PackageUsageInfoDto packageUsageInfoDto = new PackageUsageInfoDto(user);
        return packageUsageInfoDto;
    }

    public ImageData imageToImageData(Image image) {
        return new ImageData(image.getImage(), image.getContentType());
    }
}
