package car.app.api.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import car.app.api.entities.User;
import car.app.api.exceptions.ApiError;
import car.app.api.service.UserService;
import car.app.api.shared.ContextHelper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.naming.AuthenticationException;
import java.io.IOException;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final UserService userService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws IOException {
        String jwt = null;
        try {
            final String authorizationHeader = request.getHeader("Authorization");
            final JwtUtils jwtUtils = ContextHelper.getApplicationContext().getBean(JwtUtils.class);

            String username = null;

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwt = authorizationHeader.substring(7);
                username = jwtUtils.getSubject(jwt);
            }

            if (StringUtils.hasText(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userService.getByUsername(username);

                if (!JwtUtils.isTokenBlacklisted(jwt) && jwtUtils.validateToken(jwt)) {
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            user, null, user.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
            filterChain.doFilter(request, response);
        } catch (JwtException | AuthenticationException | ServletException e) {
            httpServletResponse(response);
        }
    }

    private void httpServletResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ApiError apiError = new ApiError(HttpStatus.UNAUTHORIZED, "User is not logged in!");

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String jsonError = objectMapper.writeValueAsString(apiError);
        response.getWriter().write(jsonError);
    }
}

