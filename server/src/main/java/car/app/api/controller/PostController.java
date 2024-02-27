package car.app.api.controller;

import car.app.api.controller.model.*;
import car.app.api.exceptions.BindingException;
import car.app.api.exceptions.PostCustomException;
import car.app.api.exceptions.ImageCustomException;
import car.app.api.exceptions.UserNotFoundException;
import car.app.api.service.PostService;
import car.app.api.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@SecurityRequirement(name = "Bearer Authentication")
@CrossOrigin("*")
@RequestMapping("/api/post")
@RequiredArgsConstructor
@PreAuthorize("permitAll()")
public class PostController {
    public final UserService userService;
    public final PostService postService;

    @PostMapping("/create")
    public ResponseEntity<PostResponse> create(
            @Valid @RequestBody PostRequest postRequest, BindingResult result
    ) throws UserNotFoundException, BindingException, ImageCustomException, javax.naming.AuthenticationException {
            return ResponseEntity.ok(postService.save(postRequest, userService.getAuthenticatedUser(), postRequest.getImageUrls(), result));
    }

    @PostMapping("/activate/{postId}")
    public ResponseEntity<String> activate(@PathVariable(value = "postId") String postId) throws PostCustomException {
            return ResponseEntity.ok(postService.activate(postId));
    }

    @GetMapping("/all")
    public ResponseEntity<Page<PostDetails>> list(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size
    ) {
            return ResponseEntity.ok(postService.findAll(page, size));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDetails> get(@PathVariable(name = "postId") String postId) throws PostCustomException{
        return ResponseEntity.ok(postService.get(postId));
    }

    @PostMapping("/search")
    public ResponseEntity<Page<PostDetails>> search(
            @RequestBody SearchBuilderRequest searchBuilderRequest,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size
    ) {
            return ResponseEntity.ok(postService.search(searchBuilderRequest, page, size));
    }

    @GetMapping("/user")
    public ResponseEntity<Page<PostDetails>> listByUser(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "20") Integer size
    ) throws UserNotFoundException, javax.naming.AuthenticationException {
            return ResponseEntity.ok(postService.listByUser(userService.getAuthenticatedUser().getId(), page, size));
    }

    @PutMapping("/edit/{postId}")
    public ResponseEntity<EditPostResponse> edit(@PathVariable(name = "postId") String postId, @RequestBody EditPostRequest editPostRequest, BindingResult result) throws PostCustomException, UserNotFoundException, BindingException, javax.naming.AuthenticationException, ImageCustomException {
            return ResponseEntity.ok(postService.edit(postId, editPostRequest, userService.getAuthenticatedUser(), result));
    }

    @DeleteMapping("/delete/{postId}")
    public ResponseEntity<Void> delete(@PathVariable(name = "postId") String postId) throws PostCustomException {
            postService.delete(postId);
            return ResponseEntity.ok().build();
        }
}
