package hr.algebra.postagram.ui;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import hr.algebra.postagram.configs.PasswordEncoderProvider;
import hr.algebra.postagram.helper.PackageHelper;
import hr.algebra.postagram.helper.UserHelper;
import hr.algebra.postagram.models.Package;
import hr.algebra.postagram.models.User;
import hr.algebra.postagram.services.PackageService;
import hr.algebra.postagram.services.RoleService;
import hr.algebra.postagram.services.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PlaywrightUiTests {
    static Playwright playwright;
    static Browser browser;

    @LocalServerPort
    int port;

    String baseUrl() {
        return "http://localhost:" + port;
    }

    @Autowired
    PackageService packageService;
    @Autowired
    UserService userService;
    @Autowired
    RoleService roleService;

    @BeforeEach
    void setupUser() {
        Package save = packageService.save(PackageHelper.getUserPackage());
        User defaultUser = UserHelper.getDefaultUser();
        defaultUser.setUserPackage(save);
        defaultUser.setRoles(List.of(UserHelper.getUserRole()));
        defaultUser.setPassword(PasswordEncoderProvider.getStaticPasswordEncoder().encode(defaultUser.getPassword()));
        userService.save(defaultUser);
    }

    @AfterEach
    void tearDown() {
        userService.delete(userService.findAll().getFirst());
        packageService.delete(packageService.findAll().getFirst());
    }

    @BeforeAll
    static void setup() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(false)
        );
    }

    @AfterAll
    static void teardown() {
        browser.close();
        playwright.close();
    }

    @Test
    void loginFails() {
        BrowserContext context = browser.newContext();
        Page page = context.newPage();

        page.navigate(baseUrl() + "/auth/login", new Page.NavigateOptions()
                .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

        page.fill("input[name=username]", "wrong");
        page.fill("input[name=password]", "wrong");

        page.click("button[type=submit]");

        page.waitForURL("**/auth/login?error");

        assertTrue(page.locator("#error").isVisible());

        context.close();
    }

    @Test
    void loginSucceeds() {
        BrowserContext context = browser.newContext();
        Page page = context.newPage();

        page.navigate(baseUrl() + "/auth/login", new Page.NavigateOptions()
                .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

        page.fill("input[name=username]", "user");
        page.fill("input[name=password]", "plaintextPassword");

        page.click("button[type=submit]");

        page.waitForURL("**/mvc/public/post/home");

        assertThat(page).hasURL(Pattern.compile(".*/mvc/public/post/home"));

        context.close();
    }
}
