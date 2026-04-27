package esprit.User.repositories;

import esprit.User.entities.User;
import esprit.User.entities.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    Optional<VerificationToken> findByUser(User user);
    void deleteByUser(User user);
    void deleteByExpiryDateBefore(LocalDateTime now);
}
