package esprit.User.services;

import esprit.User.dto.ChangePasswordRequest;
import esprit.User.dto.FaceIdSettingsRequest;
import esprit.User.entities.User;
import esprit.User.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class SettingsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SettingsService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Map<String, Object> changePassword(ChangePasswordRequest request) {
        if (request == null) {
            throw new RuntimeException("Corps de requete vide");
        }
        if (isBlank(request.getCurrentPassword()) || isBlank(request.getNewPassword())) {
            throw new RuntimeException("Les champs currentPassword et newPassword sont obligatoires");
        }
        if (request.getNewPassword().length() < 6) {
            throw new RuntimeException("Le nouveau mot de passe doit contenir au moins 6 caracteres");
        }

        User user = getAuthenticatedUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Mot de passe actuel incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        Map<String, Object> data = new HashMap<>();
        data.put("username", user.getUsername());
        return data;
    }

    @Transactional
    public Map<String, Object> updateFaceId(FaceIdSettingsRequest request) {
        if (request == null || request.getEnabled() == null) {
            throw new RuntimeException("Le champ enabled est obligatoire");
        }

        User user = getAuthenticatedUser();
        user.setFaceIdEnabled(request.getEnabled());
        userRepository.save(user);

        Map<String, Object> data = new HashMap<>();
        data.put("faceIdEnabled", user.getFaceIdEnabled());
        data.put("username", user.getUsername());
        return data;
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Utilisateur non authentifie");
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("Utilisateur connecte introuvable");
        }
        return user;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
