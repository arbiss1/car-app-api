package car.app.api.service;

import car.app.api.controller.model.FavoriteDetails;
import car.app.api.controller.model.FavoritesResponse;
import car.app.api.controller.model.SearchBuilderRequest;
import car.app.api.exceptions.FavoritesCustomException;
import car.app.api.exceptions.UserNotFoundException;
import car.app.api.entities.Post;
import car.app.api.repository.PostRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.AuthenticationException;
import java.util.List;
import java.util.Locale;

@Service
@AllArgsConstructor
public class FavoritesService {
    private final PostRepository postRepository;
    private final ImageUploadService imageUploadService;
    private final MessageSource messageByLocale;
    private final UserService userService;
    private final SearchService searchService;

    public FavoritesResponse add(String postId) throws FavoritesCustomException {
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new FavoritesCustomException(buildError("error.404.postNotFound"))
        );
        post.setIsFavorite(true);
        return new FavoritesResponse(postRepository.save(post).getId());
    }

    @Transactional
    public void remove(String postId) throws FavoritesCustomException {
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new FavoritesCustomException(buildError("error.404.postNotFound"))
        );
        post.setIsFavorite(false);
        postRepository.save(post);
    }

    public Page<FavoriteDetails> show(Integer page, Integer size) throws FavoritesCustomException, UserNotFoundException, AuthenticationException {
        Page<Post> response = postRepository.findAllByUserAndIsFavoriteTrue(PageRequest.of(page, size), userService.getAuthenticatedUser());
        if(response.isEmpty()){
            throw new FavoritesCustomException(buildError("error.404.noFavoritesFound"));
        }

        List<FavoriteDetails> mappedPostDetails = response.stream()
                .map(post -> mapToPostDetail(post, imageUploadService.getImages(post)
                        .stream().map(String::valueOf).toList()))
                .toList();

        return new PageImpl<>(mappedPostDetails, response.getPageable(), response.getTotalElements());
    }

    public Page<FavoriteDetails> search(SearchBuilderRequest searchBuilderRequest, Integer page, Integer size) throws UserNotFoundException, AuthenticationException {
        if (searchBuilderRequest == null) {
            Page<Post> postPage = postRepository.findAllByUserAndIsFavoriteTrue(PageRequest.of(page, size), userService.getAuthenticatedUser());
            List<FavoriteDetails> postDetailsList = postPage.getContent().stream()
                    .map(post -> mapToPostDetail(post, imageUploadService.getImages(post)
                            .stream().map(String::valueOf).toList()))
                    .toList();

            return new PageImpl<>(postDetailsList, postPage.getPageable(), postPage.getTotalElements());
        }
        Page<Post> response = searchService.searchPosts(searchBuilderRequest, page, size);
        List<FavoriteDetails> postDetailsList = response.stream()
                .map(post -> mapToPostDetail(post, imageUploadService.getImages(post)
                        .stream().map(String::valueOf).toList()))
                .toList();

        return new PageImpl<>(postDetailsList, response.getPageable(), response.getTotalElements());
    }

    public FavoriteDetails mapToPostDetail(Post post, List<String> images){
        return new FavoriteDetails(
                post.getId(),
                post.getTitle(),
                post.getDescription(),
                post.getPostType(),
                post.getCurrency(),
                post.getPostAdvertIndex(),
                post.getType(),
                post.getBrand(),
                post.getModel(),
                post.getColor(),
                post.getTransmission(),
                post.getKilometers(),
                post.getFuel(),
                post.getPower(),
                post.getPrice(),
                post.getFirstRegistration(),
                post.getEngineSize(),
                post.getCreatedAt(),
                post.getModifiedAt(),
                images
        );
    }

    private String buildError(String message) {
        return messageByLocale.getMessage(message, null, Locale.ENGLISH);
    }
}
