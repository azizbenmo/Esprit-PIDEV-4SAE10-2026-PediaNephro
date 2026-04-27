package esprit.User.repositories;

import esprit.User.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    User findByUsername(String username);

    User findByEmail(String email);

    /**
     * Recherche un utilisateur par username OU email avec une seule valeur.
     * Retourne au plus un résultat (le premier trouvé) pour éviter
     * "Query did not return a unique result" quand plusieurs utilisateurs matchent.
     */
    @Query("SELECT u FROM User u WHERE u.username = :value OR u.email = :value")
    Optional<User> findFirstByUsernameOrEmail(@Param("value") String value);

    /** @deprecated Préférer findFirstByUsernameOrEmail(String) pour éviter les doublons */
    Optional<User> findByUsernameOrEmail(String username, String email);

}

