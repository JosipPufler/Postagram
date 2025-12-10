package hr.algebra.postagram.services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2FailHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException {

        if (exception.getCause() instanceof OAuth2AuthorizationException authEx &&
                "registration_required".equals(authEx.getError().getErrorCode())) {
            request.getSession().setAttribute("oauthEmail", request.getAttribute("email"));
            response.sendRedirect("/auth/register/oauth2");
        } else {
            response.sendRedirect("/auth/login?error");
        }
    }
}
