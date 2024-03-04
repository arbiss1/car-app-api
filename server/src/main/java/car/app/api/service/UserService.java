package car.app.api.service;

import car.app.api.controller.model.EditUserRequest;
import car.app.api.controller.model.UserRequest;
import car.app.api.controller.model.UserResponse;
import car.app.api.exceptions.UserNotFoundException;
import car.app.api.exceptions.UsernameAlreadyExists;
import car.app.api.controller.model.GetUserResponse;
import car.app.api.entities.User;
import car.app.api.repository.UserRepository;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import javax.naming.AuthenticationException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

@Service
public class UserService {

    public final UserRepository userRepository;
    private final MessageSource messageByLocale;

    public UserService(UserRepository userRepository, MessageSource messageByLocale) {
        this.userRepository = userRepository;
        this.messageByLocale = messageByLocale;
    }
    PasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    public GetUserResponse get() throws UserNotFoundException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> findUser = userRepository.findByUsername(userDetails.getUsername());
        if (findUser.isEmpty()) {
            throw new UserNotFoundException(buildError("error.404.userNotFound"));
        }
        User user = findUser.get();
        return new GetUserResponse(user.getUsername(), user.getFirstName(), user.getLastName(), user.getCity(), user.getCountry(), user.getEmail(), user.getPhoneNumber(), user.getAddress());
    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public void delete() throws UserNotFoundException, AuthenticationException {
        User findUser = userRepository.findById(getAuthenticatedUser().getId()).orElseThrow(() -> new UserNotFoundException(buildError("error.404.userNotFound")));
        userRepository.deleteById(findUser.getId());
    }

    public GetUserResponse edit(EditUserRequest editUserRequest) throws UserNotFoundException, AuthenticationException {
        User user = getAuthenticatedUser();
        updateFieldIfNotNull(user::setAddress, editUserRequest.getAddress());
        updateFieldIfNotNull(user::setFirstName, editUserRequest.getFirstName());
        updateFieldIfNotNull(user::setLastName, editUserRequest.getLastName());
        updateFieldIfNotNull(user::setPhoneNumber, editUserRequest.getPhoneNumber());
        updateFieldIfNotNull(user::setCountry, editUserRequest.getCountry());
        updateFieldIfNotNull(user::setCity, editUserRequest.getCity());
        updateFieldIfNotNull(user::setEmail, editUserRequest.getEmail());
        user.setModifiedAt(LocalDateTime.now());
        user.setModifiedBy(user.getUsername());

        User savedUser = userRepository.save(user);
        return new GetUserResponse(
                savedUser.getUsername(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getCity(),
                savedUser.getCountry(),
                savedUser.getEmail(),
                savedUser.getPhoneNumber(),
                savedUser.getAddress()
        );
    }

    public UserResponse save(UserRequest userRequest, BindingResult result) throws UsernameAlreadyExists {
        if (result.hasErrors()) {
            throw new UsernameAlreadyExists(buildError(result.getAllErrors().toString()));
        }
        Optional<User> findIfUserIsIdentified = userRepository.findByUsernameOrEmailOrPhoneNumber(userRequest.getUsername(), userRequest.getEmail(), userRequest.getPhoneNumber());

        if (findIfUserIsIdentified.isPresent()) {
            throw new UsernameAlreadyExists(buildError("error.409.duplicatedInfo"));
        }

        User user = mapUser(userRequest);
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setModifiedAt(LocalDateTime.now());
        user.setCreatedAt(LocalDateTime.now());
        user.setCreatedBy(user.getUsername());
        user.setModifiedBy(user.getUsername());
        return new UserResponse(userRepository.save(user).getId());
    }

    public User getAuthenticatedUser() throws UserNotFoundException, AuthenticationException {
        UserDetails userDetails;
        try {
            userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        } catch (Exception e){
            throw new AuthenticationException(buildError("error.401.auth"));
        }
        return userRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new UserNotFoundException(buildError("error.404.userNotFound")));
    }

    public User mapUser(UserRequest userRequest) {
        return new User(
                userRequest.getUsername(),
                userRequest.getPassword(),
                userRequest.getFirstName(),
                userRequest.getLastName(),
                userRequest.getCity(),
                userRequest.getCountry(),
                userRequest.getEmail(),
                userRequest.getPhoneNumber(),
                userRequest.getAddress(),
                "USER"
        );
    }

    private String buildError(String message) {
        return messageByLocale.getMessage(message, null, Locale.ENGLISH);
    }

    private <T> void updateFieldIfNotNull(Consumer<T> setter, T value) {
        if (value != null && value != "") {
            setter.accept(value);
        }
    }
}
