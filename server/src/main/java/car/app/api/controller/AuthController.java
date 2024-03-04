package car.app.api.controller;

import car.app.api.configs.JwtUtils;
import car.app.api.controller.model.*;
import car.app.api.exceptions.UserNotFoundException;
import car.app.api.exceptions.UsernameAlreadyExists;
import car.app.api.service.UserService;
import car.app.api.service.auth.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import javax.validation.Valid;

@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin("*")
@PreAuthorize("permitAll()")
public class AuthController {
    private final UserService userService;
    private final AuthService authenticationService;
    private final JwtUtils jwtUtils;

    @PostMapping(value = "/authenticate")
    public ResponseEntity<AuthUserResponse> authenticate(@Valid @RequestBody AuthUserRequest authenticationRequest, BindingResult result) throws UsernameAlreadyExists {
        return ResponseEntity.ok(authenticationService.authenticate(authenticationRequest, result));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> save(@Valid @RequestBody UserRequest userRequest, BindingResult result) throws UsernameAlreadyExists {
        return ResponseEntity.ok(userService.save(userRequest, result));
    }

    @PutMapping("/edit")
    public ResponseEntity<GetUserResponse> edit(@RequestBody EditUserRequest editUserRequest) throws UserNotFoundException, AuthenticationException {
        return ResponseEntity.ok(userService.edit(editUserRequest));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        jwtUtils.handleLogout(extractTokenFromHeader(request));
        return ResponseEntity.ok().build();
    }

    @GetMapping()
    public ResponseEntity<GetUserResponse> get() throws UserNotFoundException {
        return ResponseEntity.ok(userService.get());
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> delete() throws UserNotFoundException, AuthenticationException {
        userService.delete();
        return ResponseEntity.ok().build();
    }

    private String extractTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
