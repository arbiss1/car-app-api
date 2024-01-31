package car.app.api.service.auth;

import car.app.api.configs.JwtUtils;
import car.app.api.controller.model.AuthUserRequest;
import car.app.api.controller.model.AuthUserResponse;
import car.app.api.exceptions.UsernameAlreadyExists;
import car.app.api.service.UserService;
import car.app.api.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {
    public final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    public final MessageSource messageByLocale;
    private final Locale locale = Locale.ENGLISH;

    public AuthUserResponse authenticate(AuthUserRequest request, BindingResult result) throws UsernameAlreadyExists {
        if (result.hasErrors()) {
            throw new UsernameAlreadyExists(
                    messageByLocale.getMessage(result.getAllErrors().toString(), null, locale)
            );
        }

       try{
           authenticationManager.authenticate(
                   new UsernamePasswordAuthenticationToken(
                           request.getUsername(),
                           request.getPassword()
                   )
           );
       }catch (Exception e){
          throw new UsernameNotFoundException("Username cannot authenticate");
       }
        User user = userService.getByUsername(request.getUsername());
        String jwtToken = jwtUtils.generateToken(user);
        return AuthUserResponse.builder()
                .accessToken(jwtToken)
                .username(user.getUsername())
                .build();
    }
}