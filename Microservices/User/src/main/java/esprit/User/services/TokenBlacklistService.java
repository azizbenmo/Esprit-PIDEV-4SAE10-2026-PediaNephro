package esprit.User.services;

import esprit.User.entities.TokenBlacklist;
import esprit.User.repositories.TokenBlacklistRepository;
import esprit.User.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final JwtService jwtService;

    @Transactional
    public void blacklistToken(String token) {
        if (tokenBlacklistRepository.existsByToken(token)) {
            return;
        }

        Date expirationDate;
        try {
            expirationDate = jwtService.extractExpiration(token);
        } catch (Exception e) {
            // If token is already expired or malformed, no need to store it.
            return;
        }

        TokenBlacklist tokenBlacklist = TokenBlacklist.builder()
                .token(token)
                .expiryDate(expirationDate)
                .build();

        tokenBlacklistRepository.save(tokenBlacklist);
    }

    public boolean isBlacklisted(String token) {
        return tokenBlacklistRepository.existsByToken(token);
    }

    // Periodic cleanup task (daily at midnight).
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void removeExpiredTokens() {
        tokenBlacklistRepository.deleteByExpiryDateBefore(new Date());
    }
}

