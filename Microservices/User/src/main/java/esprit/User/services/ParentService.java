package esprit.User.services;

import esprit.User.dto.ParentRegistrationDto;
import esprit.User.dto.ParentUpdateDto;
import esprit.User.entities.Parent;
import esprit.User.entities.Role;
import esprit.User.entities.User;
import esprit.User.repositories.ParentRepository;
import esprit.User.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ParentService {

    private static final Logger log = LoggerFactory.getLogger(ParentService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Autowired
    private KeycloakService keycloakService;

    @Transactional
    public Parent createParentWithUser(ParentRegistrationDto dto) {
        validateCreateRequest(dto);

        if (userRepository.findByEmail(dto.getEmail()) != null) {
            throw new RuntimeException("Un utilisateur avec cet email existe deja: " + dto.getEmail());
        }

        if (userRepository.findByUsername(dto.getUsername()) != null) {
            throw new RuntimeException("Un utilisateur avec ce username existe deja: " + dto.getUsername());
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.PARENT);
        user.setEnabled(false);
        user = userRepository.save(user);

        Parent parent = new Parent();
        parent.setFullName(dto.getFullName());
        parent.setPhone(dto.getPhone());
        parent.setAddress(dto.getAddress());
        parent.setUser(user);
        user.setParent(parent);

        Parent savedParent = parentRepository.save(parent);

        // Synchroniser avec Keycloak (non-bloquant)
        try {
            keycloakService.createUserInKeycloak(dto.getUsername(), dto.getEmail(), dto.getPassword(), Role.PARENT);
        } catch (Exception ex) {
            log.warn("[Keycloak] Sync parent '{}' échouée (non bloquant): {}", dto.getEmail(), ex.getMessage());
        }

        try {
            emailVerificationService.createAndSendVerificationToken(user);
        } catch (RuntimeException ex) {
            // University/dev fallback: do not fail registration if SMTP fails.
            log.warn("Email verification sending failed for {}. Fallback enabling account. Cause: {}",
                    user.getEmail(), ex.getMessage());
            user.setEnabled(true);
            userRepository.save(user);
        }

        return savedParent;
    }

    @Transactional
    public Parent updateParentWithUser(Long id, ParentUpdateDto dto) {
        Parent parent = parentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Parent introuvable avec id=" + id));

        User user = parent.getUser();
        if (user == null) {
            throw new RuntimeException("User associe introuvable pour le parent id=" + id);
        }

        if (dto.getUsername() != null && !dto.getUsername().isEmpty()) {
            User existingUser = userRepository.findByUsername(dto.getUsername());
            if (existingUser != null && !existingUser.getId().equals(user.getId())) {
                throw new RuntimeException("Un utilisateur avec ce username existe deja: " + dto.getUsername());
            }
            user.setUsername(dto.getUsername());
        }

        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
            User existingUser = userRepository.findByEmail(dto.getEmail());
            if (existingUser != null && !existingUser.getId().equals(user.getId())) {
                throw new RuntimeException("Un utilisateur avec cet email existe deja: " + dto.getEmail());
            }
            user.setEmail(dto.getEmail());
        }

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getActive() != null) {
            user.setActive(dto.getActive());
        }

        userRepository.save(user);

        if (dto.getFullName() != null && !dto.getFullName().isEmpty()) {
            parent.setFullName(dto.getFullName());
        }

        if (dto.getPhone() != null && !dto.getPhone().isEmpty()) {
            parent.setPhone(dto.getPhone());
        }

        if (dto.getAddress() != null && !dto.getAddress().isEmpty()) {
            parent.setAddress(dto.getAddress());
        }

        return parentRepository.save(parent);
    }

    public List<Parent> findAll() {
        return parentRepository.findAll();
    }

    public Parent findById(Long id) {
        return parentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Parent introuvable avec id=" + id));
    }

    @Transactional
    public void delete(Long id) {
        Parent parent = parentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Parent introuvable avec id=" + id));

        User user = parent.getUser();
        if (user != null) {
            parentRepository.deleteById(id);
            userRepository.deleteById(user.getId());
        } else {
            parentRepository.deleteById(id);
        }
    }

    private void validateCreateRequest(ParentRegistrationDto dto) {
        if (dto == null) {
            throw new RuntimeException("Corps de requete vide");
        }
        if (isBlank(dto.getUsername()) || isBlank(dto.getEmail()) || isBlank(dto.getPassword())
                || isBlank(dto.getFullName()) || isBlank(dto.getPhone())) {
            throw new RuntimeException("Tous les champs obligatoires doivent etre remplis");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
