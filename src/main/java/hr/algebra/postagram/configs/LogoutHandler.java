package hr.algebra.postagram.configs;

import hr.algebra.postagram.models.events.LogoutEvent;
import hr.algebra.postagram.services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LogoutHandler implements LogoutSuccessHandler {
    private final ApplicationEventPublisher publisher;
    private final UserService userService;

    public LogoutHandler(ApplicationEventPublisher publisher, UserService userService){
        this.publisher = publisher;
        this.userService = userService;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        Cookie jwtCookie = new Cookie("JWT_TOKEN", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);

        userService.findByUsername(authentication.getName()).ifPresent(user -> publisher.publishEvent(new LogoutEvent(user)));

        response.sendRedirect("/login?logout");
    }
}
