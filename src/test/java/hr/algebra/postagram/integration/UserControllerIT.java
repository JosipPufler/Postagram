package hr.algebra.postagram.integration;

import hr.algebra.postagram.helper.PackageHelper;
import hr.algebra.postagram.helper.UserHelper;
import hr.algebra.postagram.models.Package;
import hr.algebra.postagram.models.User;
import hr.algebra.postagram.repositories.PackageRepo;
import hr.algebra.postagram.repositories.UserRepo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PackageRepo packageRepository;

    @Autowired
    private UserRepo userRepository;

    static Package aPackage;

    @BeforeAll
    static void beforeAll(@Autowired PackageRepo packageRepo) {
        aPackage = packageRepo.saveAndFlush(PackageHelper.getUserPackage());
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldShowRegisterForm() throws Exception {
        mockMvc.perform(get("/auth/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("packages"))
                .andExpect(model().attributeExists("registrationForm"));
    }

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .param("username", "john")
                        .param("password", "1234")
                        .param("confirmPassword", "1234")
                        .param("email", "john@example.com")
                        .param("packageId", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login?registered"));

        Optional<User> created = userRepository.findByUsername("john");
        assertTrue(created.isPresent());
        assertEquals("john@example.com", created.get().getEmail());
        assertEquals("john", created.get().getUsername());
    }

    @Test
    void shouldRegisterUserUnsuccessfullyDueToDuplicateUsername() throws Exception {
        userRepository.saveAndFlush(UserHelper.getDefaultUser());

        mockMvc.perform(post("/auth/register")
                        .param("username", "user")
                        .param("password", "1234")
                        .param("confirmPassword", "1234")
                        .param("email", "john@example.com")
                        .param("packageId", "1")
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful());

        assertEquals(1, userRepository.count());
    }

    @Test
    void shouldRedirectToLoginOnUnauthorizedProfileAccess() throws Exception {
        mockMvc.perform(get("/auth/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
    }
}
