package ecommerce.web.app.domain.repository;

import ecommerce.web.app.entity.ImageUpload;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageUploadRepository extends JpaRepository<ImageUpload,Long> {

    ImageUpload deleteById(long id);
}
