package car.app.api.service;

import car.app.api.controller.model.*;
import car.app.api.entities.ImageUpload;
import car.app.api.entities.Post;
import car.app.api.entities.User;
import car.app.api.exceptions.BindingException;
import car.app.api.exceptions.ImageCustomException;
import car.app.api.exceptions.PostCustomException;
import car.app.api.controller.enums.PostStatus;
import car.app.api.enums.AdvertIndex;
import car.app.api.repository.PostRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
public class PostService {
    public final PostRepository postRepository;
    public final SearchService searchService;
    public final MessageSource messageByLocale;
    public final ImageUploadService imageUploadService;

    public PostResponse save(PostRequest postRequest, User userAuth, List<String> postsImageUrls, BindingResult result
    ) throws BindingException, ImageCustomException {
        if (result.hasErrors()) {
            throw new BindingException(result.getAllErrors().toString());
        }
        Post post = postRepository.save(mapToPost(postRequest, userAuth));
        if(postsImageUrls != null && !postsImageUrls.isEmpty()) imageUploadService.postImageUpload(postsImageUrls, post);
        return new PostResponse(post.getId());
    }

    public PostDetails get(String postId) throws PostCustomException{
        Post post = postRepository.findById(postId).orElseThrow(() -> new PostCustomException(buildError("error.404.postNotFound")));
        return mapToPostDetail(post, imageUploadService.getImages(post).stream().map(ImageUpload::getProfileImage).toList());
    }

    public EditPostResponse edit(String postId, EditPostRequest editPostRequest, User authUser, BindingResult result)
            throws PostCustomException, BindingException, ImageCustomException {
        if (result.hasErrors()) {
            throw new BindingException(result.getAllErrors().toString());
        }
        Optional<Post> findPost = postRepository.findById(postId);
        if (findPost.isPresent()) {
            Post editablePost = edit(findPost.get(), editPostRequest, authUser);
            editablePost.setIsFavorite(findPost.get().getIsFavorite());
            editablePost.setId(findPost.get().getId());
            return new EditPostResponse(postRepository.save(editablePost).getId());
        } else {
            throw new PostCustomException(buildError("error.409.postServerError"));
        }
    }

    public String activate(String postId) throws PostCustomException {
        Optional<Post> findIfPostExist = postRepository.findById(postId);
        if (findIfPostExist.isPresent()) {
            findIfPostExist.get().setStatus(PostStatus.ACTIVE.mapToStatus());
        } else {
            throw new PostCustomException(buildError("error.404.postNotFound"));
        }
        return postRepository.save(findIfPostExist.get()).getId();
    }

    public Page<PostDetails> findAll(Integer page, Integer size) {
        Page<Post> postPage = postRepository.findAll(PageRequest.of(page, size));
        List<PostDetails> postDetailsList = new ArrayList<>(postPage.getContent().stream()
                .map(post -> mapToPostDetail(post, imageUploadService.getImages(post)
                        .stream().map(ImageUpload::getProfileImage).toList()))
                .toList());

        postDetailsList.sort(Comparator.comparing(PostDetails::getCreatedAt));
        return new PageImpl<>(postDetailsList, postPage.getPageable(), postPage.getTotalElements());
    }


    public Page<PostDetails> search(SearchBuilderRequest searchBuilderRequest, Integer page, Integer size) {
        if (searchBuilderRequest == null) {
            Page<Post> postPage = postRepository.findAll(PageRequest.of(page, size));
            List<PostDetails> postDetailsList = postPage.getContent().stream()
                    .map(post -> mapToPostDetail(post, imageUploadService.getImages(post)
                            .stream().map(ImageUpload::getProfileImage).toList()))
                    .toList();

            return new PageImpl<>(postDetailsList, postPage.getPageable(), postPage.getTotalElements());
        }
        Page<Post> response = searchService.searchPosts(searchBuilderRequest, page, size);
        List<PostDetails> postDetailsList = response.stream()
                .map(post -> mapToPostDetail(post, imageUploadService.getImages(post)
                        .stream().map(ImageUpload::getProfileImage).toList()))
                .toList();

        return new PageImpl<>(postDetailsList, response.getPageable(), response.getTotalElements());
    }

    public Page<PostDetails> listByUser(String userId, Integer page, Integer size) {
        Page<Post> postPage = postRepository.findByUserId(userId, PageRequest.of(page, size));
        List<PostDetails> postDetailsList = postPage.getContent().stream()
                .map(post -> mapToPostDetail(post, imageUploadService.getImages(post)
                        .stream().map(ImageUpload::getProfileImage).toList()))
                .toList();

        return new PageImpl<>(postDetailsList, postPage.getPageable(), postPage.getTotalElements());
    }

    public void delete(String postId) throws PostCustomException {
        Post findPost = postRepository.findById(postId).orElseThrow(() ->
                new PostCustomException(buildError("error.404.postNotFound")));
        imageUploadService.deleteImages(findPost);
        postRepository.deleteById(findPost.getId());
    }

    private Post mapToPost(PostRequest postRequest, User getAuthenticatedUser){
        Post post = new Post();
        post.setUser(getAuthenticatedUser);
        post.setCreatedAt(LocalDateTime.now());
        post.setCreatedBy(getAuthenticatedUser.getUsername());
        post.setModifiedBy(getAuthenticatedUser.getUsername());
        post.setModifiedAt(LocalDateTime.now());
        post.setStatus(PostStatus.PENDING.mapToStatus());
        post.setPostAdvertIndex(postRequest.getPostAdvertIndex() != null ? postRequest.getPostAdvertIndex().mapToStatus() : AdvertIndex.FREE);
        post.setCurrency(postRequest.getCurrency().mapToStatus());
        post.setDescription(postRequest.getDescription());
        post.setPrice(postRequest.getPrice());
        post.setTitle(postRequest.getTitle());
        post.setBrand(postRequest.getBrand());
        post.setEngineSize(postRequest.getEngineSize());
        post.setFirstRegistration(postRequest.getFirstRegistration());
        post.setFuel(postRequest.getFuel());
        post.setColor(postRequest.getColor());
        post.setKilometers(postRequest.getKilometers());
        post.setPower(postRequest.getPower());
        post.setTransmission(postRequest.getTransmission());
        post.setType(postRequest.getType());
        post.setModel(postRequest.getModel());
        post.setPostType(postRequest.getPostType());
        post.setIsFavorite(false);
        return post;
    }

    private Post edit(Post post, EditPostRequest editPostRequest, User getAuthenticatedUser) throws ImageCustomException {
        post.setModifiedBy(getAuthenticatedUser.getUsername());
        post.setModifiedAt(LocalDateTime.now());
        if(editPostRequest.getCurrency() != null) post.setCurrency(editPostRequest.getCurrency().mapToStatus());
        if(editPostRequest.getDescription() != null) post.setDescription(editPostRequest.getDescription());
        if(editPostRequest.getPrice() != null) post.setPrice(editPostRequest.getPrice());
        if(editPostRequest.getTitle() != null) post.setTitle(editPostRequest.getTitle());
        if(editPostRequest.getBrand() != null) post.setBrand(editPostRequest.getBrand());
        if(editPostRequest.getEngineSize() != null) post.setEngineSize(editPostRequest.getEngineSize());
        if(editPostRequest.getFirstRegistration() != null) post.setFirstRegistration(editPostRequest.getFirstRegistration());
        if(editPostRequest.getFuel() != null) post.setFuel(editPostRequest.getFuel());
        if(editPostRequest.getColor() != null) post.setColor(editPostRequest.getColor());
        if(editPostRequest.getKilometers() != null) post.setKilometers(editPostRequest.getKilometers());
        if(editPostRequest.getPower() != null) post.setPower(editPostRequest.getPower());
        if(editPostRequest.getTransmission() != null) post.setTransmission(editPostRequest.getTransmission());
        if(editPostRequest.getType() != null) post.setType(editPostRequest.getType());
        if(editPostRequest.getModel() != null) post.setModel(editPostRequest.getModel());
        if(editPostRequest.getPostType() != null) post.setPostType(editPostRequest.getPostType());
        if(!editPostRequest.getImageUrls().isEmpty() && editPostRequest.getImageUrls() != null) {
            List<String> existingImages = imageUploadService.getImages(post).stream().map(ImageUpload::getProfileImage).toList();
            List<String> toBeSaved = new ArrayList<>();
            for (String imageUrl : editPostRequest.getImageUrls()) {
                if(!existingImages.contains(imageUrl)){
                    toBeSaved.add(imageUrl);
                }
            }
            imageUploadService.postImageUpload(toBeSaved, post);
        }
        return post;
    }

    public PostDetails mapToPostDetail(Post post, List<String> images){
        return new PostDetails(
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
