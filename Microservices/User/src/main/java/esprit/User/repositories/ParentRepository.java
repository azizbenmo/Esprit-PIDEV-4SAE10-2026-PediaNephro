package esprit.User.repositories;

import esprit.User.entities.Parent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParentRepository extends JpaRepository<Parent, Long> {
    Optional<Parent> findByUser_Id(Long userId);

    Optional<Parent> findByUserUsername(String username);
}


