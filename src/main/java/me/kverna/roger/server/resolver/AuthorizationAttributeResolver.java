package me.kverna.roger.server.resolver;

import me.kverna.roger.server.annotation.LoggedIn;
import me.kverna.roger.server.data.User;
import me.kverna.roger.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * Attribute resolver for injecting the logged in user in controller mappings.
 */
@Component
public class AuthorizationAttributeResolver implements HandlerMethodArgumentResolver {

    private UserService userService;

    @Autowired
    public AuthorizationAttributeResolver(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        // Inject User types annotated with @LoggedIn
        return methodParameter.getParameterType().equals(User.class) && methodParameter.getParameterAnnotation(LoggedIn.class) != null;
    }

    @Override
    public User resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        // Get the email of the logged in user from the AuthorizationInterceptor preHandler and resolve the user object
        String email = (String) Objects.requireNonNull(nativeWebRequest.getNativeRequest(HttpServletRequest.class)).getAttribute("loggedInEmail");
        return userService.getUser(email);
    }
}
