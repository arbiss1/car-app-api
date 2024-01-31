package car.app.api.repository;

import car.app.api.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsernameOrEmailOrPhoneNumber(String username, String email, String phoneNumber);
    Optional<User> findByUsername(String username);
}
