package esprit.User.controller;

import esprit.User.dto.UserPublicSummaryDto;
import esprit.User.entities.User;
import esprit.User.repositories.UserRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lecture utilisateur pour les autres microservices (Feign, etc.).
 * Exposé sur un chemin dédié ; à protéger au niveau réseau / gateway en production.
 */
@RestController
@RequestMapping(value = "/mic1/internal/users", produces = MediaType.APPLICATION_JSON_VALUE)
public class InternalUserController {

    private final UserRepository userRepository;

    public InternalUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserPublicSummaryDto> getById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(this::toSummary)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private UserPublicSummaryDto toSummary(User u) {
        return new UserPublicSummaryDto(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getRole() != null ? u.getRole().name() : null,
                u.getActive()
        );
    }
}
