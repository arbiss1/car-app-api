package car.app.api.repository;

import car.app.api.entities.ImageUpload;
import car.app.api.entities.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageUploadRepository extends JpaRepository<ImageUpload, String> {
    List<ImageUpload> findAllByPost(Post post);

    void deleteAllByPost(Post post);
}
