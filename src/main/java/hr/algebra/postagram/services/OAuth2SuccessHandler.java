package hr.algebra.postagram.services;

import hr.algebra.postagram.models.CustomUserDetails;
import hr.algebra.postagram.models.User;
import hr.algebra.postagram.models.events.LoginEvent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    public OAuth2SuccessHandler(UserService userService, ApplicationEventPublisher eventPublisher) {
        this.userService = userService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String login = oAuth2User.getAttribute("login");
        String username = oAuth2User.getAttribute("name");
        String email = oAuth2User.getAttribute("email");

        if (username == null){
            username = login;
        }

        Optional<User> byUsername = userService.findByUsername(username);
        if (byUsername.isEmpty()) {
            request.getSession().setAttribute("oauthUsername", username);
            if (email != null){
                request.getSession().setAttribute("oauthEmail", email);
            }
            response.sendRedirect("/auth/register/oauth2");
            return;
        }

        CustomUserDetails customUserDetails = new CustomUserDetails(byUsername.get());
        Authentication newAuth =
                new UsernamePasswordAuthenticationToken(
                        customUserDetails,
                        null,
                        customUserDetails.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(newAuth);

        String ipAddress = request.getRemoteAddr();
        User user = byUsername.get();
        eventPublisher.publishEvent(new LoginEvent(user, ipAddress));

        response.sendRedirect("/mvc/public/post/home");
    }
}
