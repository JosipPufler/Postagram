package hr.algebra.postagram.configs;

import hr.algebra.postagram.models.User;
import hr.algebra.postagram.models.events.LoginEvent;
import hr.algebra.postagram.services.JwtService;
import hr.algebra.postagram.services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class LoginHandler implements AuthenticationSuccessHandler {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private final ApplicationEventPublisher publisher;

    public LoginHandler(JwtService jwtService, UserDetailsService userDetailsService, UserService userService, ApplicationEventPublisher publisher) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
        this.publisher = publisher;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String username = authentication.getName();
        Optional<User> byUsername = userService.findByUsername(username);
        if (byUsername.isEmpty()) {
            response.sendRedirect("/auth/login");
            return;
        }

        String ipAddress = request.getRemoteAddr();
        publisher.publishEvent(new LoginEvent(byUsername.get(), ipAddress));

        String token = jwtService.generateJwtToken(userDetailsService.loadUserByUsername(username), byUsername.get().getId());

        Cookie jwtCookie = new Cookie("JWT_TOKEN", token);
        jwtCookie.setHttpOnly(false);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(24 * 60 * 60); // 1 day
        response.addCookie(jwtCookie);

        response.sendRedirect("/mvc/public/post/home");
    }
}
