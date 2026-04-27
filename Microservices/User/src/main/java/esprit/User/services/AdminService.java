package esprit.User.services;

import esprit.User.entities.Admin;
import esprit.User.entities.Role;
import esprit.User.entities.User;
import esprit.User.dto.AdminRegistrationDto;
import esprit.User.dto.AdminUpdateDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import esprit.User.repositories.AdminRepository;
import esprit.User.repositories.UserRepository;

import java.util.List;

@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private KeycloakService keycloakService;

    @Transactional
    public Admin createAdminWithUser(AdminRegistrationDto dto) {
        // Vérifier si l'email existe déjà
        if (userRepository.findByEmail(dto.getEmail()) != null) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà: " + dto.getEmail());
        }

        // Vérifier si le username existe déjà
        if (userRepository.findByUsername(dto.getUsername()) != null) {
            throw new RuntimeException("Un utilisateur avec ce username existe déjà: " + dto.getUsername());
        }

        // 1) créer le User
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.ADMIN);
        user = userRepository.save(user);

        // 3) Synchroniser avec Keycloak (non-bloquant en cas d'erreur)
        try {
            keycloakService.createUserInKeycloak(dto.getEmail(), dto.getEmail(), dto.getPassword(), Role.ADMIN);
        } catch (Exception ex) {
            log.warn("[Keycloak] Sync admin '{}' échouée (non bloquant): {}", dto.getEmail(), ex.getMessage());
        }

        // 2) créer l'Admin
        Admin admin = new Admin();
        admin.setFullName(dto.getFullName());
        admin.setPhone(dto.getPhone());
        admin.setPosition(dto.getPosition());
        admin.setUser(user);

        return adminRepository.save(admin);
    }

    @Transactional
    public Admin updateAdminWithUser(Long id, AdminUpdateDto dto) {
        // Récupérer l'admin existant
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin introuvable avec id=" + id));

        // Récupérer le user associé
        User user = admin.getUser();
        if (user == null) {
            throw new RuntimeException("User associé introuvable pour l'admin id=" + id);
        }

        // Mettre à jour les champs User si fournis
        if (dto.getUsername() != null && !dto.getUsername().isEmpty()) {
            // Vérifier si le nouveau username n'est pas déjà utilisé par un autre user
            User existingUser = userRepository.findByUsername(dto.getUsername());
            if (existingUser != null && !existingUser.getId().equals(user.getId())) {
                throw new RuntimeException("Un utilisateur avec ce username existe déjà: " + dto.getUsername());
            }
            user.setUsername(dto.getUsername());
        }

        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
            // Vérifier si le nouveau email n'est pas déjà utilisé par un autre user
            User existingUser = userRepository.findByEmail(dto.getEmail());
            if (existingUser != null && !existingUser.getId().equals(user.getId())) {
                throw new RuntimeException("Un utilisateur avec cet email existe déjà: " + dto.getEmail());
            }
            user.setEmail(dto.getEmail());
        }

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getActive() != null) {
            user.setActive(dto.getActive());
        }

        // Sauvegarder le User mis à jour
        userRepository.save(user);

        // Mettre à jour les champs Admin si fournis
        if (dto.getFullName() != null && !dto.getFullName().isEmpty()) {
            admin.setFullName(dto.getFullName());
        }

        if (dto.getPhone() != null && !dto.getPhone().isEmpty()) {
            admin.setPhone(dto.getPhone());
        }

        if (dto.getPosition() != null && !dto.getPosition().isEmpty()) {
            admin.setPosition(dto.getPosition());
        }

        // Sauvegarder l'Admin mis à jour
        return adminRepository.save(admin);
    }

    public List<Admin> findAll() {
        return adminRepository.findAll();
    }

    public Admin findById(Long id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin introuvable avec id=" + id));
    }

    @Transactional
    public void delete(Long id) {
        // Récupérer l'admin
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin introuvable avec id=" + id));

        // Récupérer le user associé
        User user = admin.getUser();
        if (user != null) {
            // Supprimer d'abord l'admin
            adminRepository.deleteById(id);
            // Puis supprimer le user
            userRepository.deleteById(user.getId());
        } else {
            // Si pas de user associé, supprimer juste l'admin
            adminRepository.deleteById(id);
        }
    }
}


