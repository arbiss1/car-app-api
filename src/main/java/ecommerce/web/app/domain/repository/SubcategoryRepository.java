package ecommerce.web.app.domain.repository;

import ecommerce.web.app.domain.model.Subcategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubcategoryRepository extends JpaRepository<Subcategory,Long> {
}