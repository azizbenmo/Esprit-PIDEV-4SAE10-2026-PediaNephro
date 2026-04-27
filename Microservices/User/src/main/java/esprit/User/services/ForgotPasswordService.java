package esprit.User.services;

import esprit.User.entities.PasswordResetToken;
import esprit.User.entities.User;
import esprit.User.repositories.PasswordResetTokenRepository;
import esprit.User.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service pour gérer la réinitialisation de mot de passe.
 * 
 * Workflow:
 * 1. L'utilisateur demande une réinitialisation avec son email
 * 2. Un token unique est généré et stocké en base (valide 15 min)
 * 3. Un email est envoyé avec le lien de réinitialisation
 * 4. L'utilisateur clique sur le lien et entre un nouveau mot de passe
 * 5. Le token est vérifié, le mot de passe est mis à jour, le token est supprimé
 */
@Service
public class ForgotPasswordService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.password-reset.expiration-minutes:15}")
    private int tokenExpirationMinutes;

    public ForgotPasswordService(UserRepository userRepository,
                                  PasswordResetTokenRepository tokenRepository,
                                  EmailService emailService,
                                  PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Étape 1: Demande de réinitialisation de mot de passe.
     * Génère un token et envoie un email avec le lien.
     * 
     * @param email L'email de l'utilisateur
     * @throws RuntimeException si l'email n'existe pas
     */
    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("Aucun compte associé à cet email: " + email);
        }

        tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken(token, user, tokenExpirationMinutes);
        tokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(email, token);
    }

    /**
     * Étape 2: Réinitialisation effective du mot de passe.
     * Vérifie le token et met à jour le mot de passe.
     * 
     * @param token Le token reçu par email
     * @param newPassword Le nouveau mot de passe (en clair)
     * @throws RuntimeException si le token est invalide ou expiré
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token invalide ou inexistant"));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new RuntimeException("Le token a expiré. Veuillez faire une nouvelle demande.");
        }

        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("Le mot de passe doit contenir au moins 6 caractères");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.delete(resetToken);
    }

    /**
     * Vérifie si un token est valide (existe et non expiré).
     * Utile pour le frontend avant d'afficher le formulaire.
     * 
     * @param token Le token à vérifier
     * @return true si le token est valide
     */
    public boolean isTokenValid(String token) {
        return tokenRepository.findByToken(token)
                .map(t -> !t.isExpired())
                .orElse(false);
    }

    /**
     * Nettoie les tokens expirés (peut être appelé par un scheduler).
     */
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}
