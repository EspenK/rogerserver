package me.kverna.roger.server.security;

import me.kverna.roger.server.annotation.Authorized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * An interceptor dealing with authorization using JSON web tokens.
 */
@Component
public class AuthorizationInterceptor extends HandlerInterceptorAdapter {

    private JwtManager jwtManager;

    @Autowired
    public AuthorizationInterceptor(JwtManager jwtManager) {
        this.jwtManager = jwtManager;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // Check if the mapping requires authorization
        Authorized authorized = handlerMethod.getMethodAnnotation(Authorized.class);
        if (authorized == null) {
            return true;
        }

        // Attempt to find a JSON web token in the request headers
        String token = parseToken(request);
        if (token == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        // Verify the token and parse the email
        String email = jwtManager.getSubject(token);
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        // Add the token back to the response and add the email as the logged in user
        response.addHeader("Authorization", "Bearer " + token);
        request.setAttribute("loggedInEmail", email);

        return true;
    }

    /**
     * Attempts to find a JSON web token in the request headers.
     *
     * @param request the request object
     * @return a pure JSON web token or null if it was not supplied
     */
    private String parseToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return null;
        }

        return token.replace("Bearer ", "").trim();
    }
}
