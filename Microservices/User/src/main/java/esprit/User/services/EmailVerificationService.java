package esprit.User.services;

import esprit.User.entities.User;
import esprit.User.entities.VerificationToken;
import esprit.User.repositories.UserRepository;
import esprit.User.repositories.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EmailVerificationService {

    private final VerificationTokenRepository verificationTokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${app.email-verification.expiration-hours:24}")
    private int verificationTokenExpirationHours;

    public EmailVerificationService(VerificationTokenRepository verificationTokenRepository,
                                    UserRepository userRepository,
                                    EmailService emailService) {
        this.verificationTokenRepository = verificationTokenRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Transactional
    public void createAndSendVerificationToken(User user) {
        verificationTokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, user, verificationTokenExpirationHours);
        verificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(user.getEmail(), token);
    }

    @Transactional
    public String verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token invalide"));

        if (verificationToken.isExpired()) {
            verificationTokenRepository.delete(verificationToken);
            throw new RuntimeException("Token expire");
        }

        User user = verificationToken.getUser();
        if (Boolean.TRUE.equals(user.getEnabled())) {
            verificationTokenRepository.delete(verificationToken);
            throw new RuntimeException("Compte deja active");
        }

        user.setEnabled(true);
        userRepository.save(user);
        verificationTokenRepository.delete(verificationToken);
        return "Email verifie avec succes";
    }

    @Transactional
    public void cleanupExpiredTokens() {
        verificationTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }
}
