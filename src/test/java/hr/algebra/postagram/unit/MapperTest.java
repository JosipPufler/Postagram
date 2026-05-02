package hr.algebra.postagram.unit;

import hr.algebra.postagram.configs.PasswordEncoderProvider;
import hr.algebra.postagram.helper.PackageHelper;
import hr.algebra.postagram.helper.UserHelper;
import hr.algebra.postagram.models.*;
import hr.algebra.postagram.models.dtos.EventDto;
import hr.algebra.postagram.models.dtos.PostDto;
import hr.algebra.postagram.models.dtos.RegistrationForm;
import hr.algebra.postagram.models.dtos.UserDto;
import hr.algebra.postagram.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

@ActiveProfiles("test")
@SpringBootTest
class MapperTest {
    @Autowired
    private Mapper mapper;

    @MockitoBean
    private RoleService roleService;

    @MockitoBean
    private HashtagService hashtagService;

    @MockitoBean
    private PackageService packageService;

    @MockitoBean
    private ImageStorageRouter imageStorageRouter;

    @MockitoBean
    private UserService userService;

    @Test
    void givenUser_whenMapUserToDto_thenReturnUserDto() {
        // Arrange
        User user = UserHelper.getDefaultUserWithId();
        UserDto expected = UserDto.builder()
                .id(user.getId())
                .packageId(user.getUserPackage().getId())
                .packageName(user.getUserPackage().getName())
                .email(user.getEmail())
                .username(user.getUsername())
                .build();

        // Act
        UserDto result = mapper.userToDto(user);

        // Assert
        assertEquals(expected, result);
    }

    @Test
    void givenRegistrationForm_whenMapFormToUser_thenReturnUser() {
        // Arrange
        RegistrationForm registrationForm = new RegistrationForm("user", "pass", "pass", "user@gmail.com", PackageHelper.getUserPackageWithId().getId());
        User expected = User.builder()
                .username(registrationForm.getUsername())
                .password(PasswordEncoderProvider.getStaticPasswordEncoder().encode(registrationForm.getPassword()))
                .email(registrationForm.getEmail())
                .active(true)
                .userPackage(PackageHelper.getUserPackageWithId())
                .uploadCount(0)
                .uploadedAmount(0L)
                .roles(List.of(UserHelper.USER_ROLE))
                .build();

        // Act
        when(packageService.findById(1L)).thenReturn(Optional.of(PackageHelper.getUserPackageWithId()));
        when(roleService.findByEnum(RoleEnum.USER)).thenReturn(UserHelper.USER_ROLE);
        User result = mapper.registerFormToUser(registrationForm);

        // Assert
        assertEquals(expected.getUsername(), result.getUsername());
        assertNotEquals(expected.getPassword(), result.getPassword());
        assertEquals(expected.getUserPackage(), result.getUserPackage());
        assertEquals(expected.getEmail(), result.getEmail());
        assertEquals(expected.getRoles().getFirst(), result.getRoles().getFirst());
        assertEquals(expected.getRoles().size(), result.getRoles().size());
    }

    @Test
    void givenEvent_whenMapEventToDto_thenReturnEventDto() {
        // Arrange
        LocalDateTime eventOccurrence = LocalDateTime.now();
        EventType eventType = new EventType(EventTypeEnum.LOGIN.name());
        String description = "description";
        User defaultUserWithId = UserHelper.getDefaultUserWithId();
        Event event = Event.builder()
                .id(1L)
                .user(defaultUserWithId.getId())
                .time(eventOccurrence)
                .eventType(eventType)
                .description(description)
                .build();
        EventDto expected = EventDto.builder()
                .id(event.getId())
                .description(description)
                .username(defaultUserWithId.getUsername())
                .userId(defaultUserWithId.getId())
                .eventType(eventType.getName())
                .createdAt(eventOccurrence)
                .build();

        // Act
        when(userService.findById(defaultUserWithId.getId())).thenReturn(Optional.of(defaultUserWithId));
        EventDto result = mapper.eventToDto(event);

        // Assert
        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getUsername(), result.getUsername());
        assertEquals(expected.getUserId(), result.getUserId());
        assertEquals(expected.getDescription(), result.getDescription());
        assertEquals(expected.getCreatedAt(), result.getCreatedAt());
        assertEquals(expected.getEventType(), result.getEventType());
    }

    @Test
    void givenPost_whenMapPostToDto_thenReturnPostDto() {
        // Arrange
        LocalDateTime postedAt = LocalDateTime.now();
        Set<Hashtag> hashtags = new HashSet<Hashtag>(Arrays.asList(
                new Hashtag(1L, "Hashtag1"),
                new Hashtag(2L, "Hashtag2")));
        String description = "description";
        String imageId = "path/to/image";
        int imageWidth = 2;
        int imageHeight = 1;
        Post post = Post.builder()
                .id(1L)
                .user(UserHelper.getDefaultUserWithId())
                .postedAt(postedAt)
                .hashtags(hashtags)
                .imageId(imageId)
                .imageWidth(imageWidth)
                .imageHeight(imageHeight)
                .aspectRatio((double)imageWidth/imageHeight)
                .description(description)
                .build();
        PostDto expected = PostDto.builder()
                .id(post.getId())
                .description(description)
                .postTime(post.getPostedAt())
                .aspectRatio(post.getAspectRatio())
                .image(imageId)
                .storageType(post.getStorageType())
                .author(post.getUser().getUsername())
                .hashtags(post.getHashtags().stream().map(Hashtag::toString).toList())
                .build();

        // Act
        PostDto result = mapper.postToDto(post);

        // Assert
        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getImage(), result.getImage());
        assertEquals(expected.getAuthor(), result.getAuthor());
        assertEquals(expected.getDescription(), result.getDescription());
        assertEquals(expected.getPostTime(), result.getPostTime());
        assertEquals(expected.getAspectRatio(), result.getAspectRatio());
        assertEquals(expected.getStorageType(), result.getStorageType());
        assertEquals(expected.getHashtags().size(), result.getHashtags().size());
        assertEquals(expected.getHashtags(), result.getHashtags());
    }
}
