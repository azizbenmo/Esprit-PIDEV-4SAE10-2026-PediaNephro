package esprit.User.services;

import esprit.User.entities.Parent;
import esprit.User.entities.Patient;
import esprit.User.entities.Role;
import esprit.User.entities.User;
import esprit.User.pedianephro.SearchSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import esprit.User.repositories.*;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User ajouterUser(User u) {
        if (u.getRole() == null) {
            u.setRole(Role.PATIENT);
        }
        u.setPassword(passwordEncoder.encode(u.getPassword()));

        User savedUser = userRepository.save(u);

        if (savedUser.getRole() == Role.PARENT && !parentRepository.existsById(savedUser.getId())) {
            Parent parent = new Parent();
            parent.setUser(savedUser);
            if (savedUser.getUsername() != null && !savedUser.getUsername().isBlank()) {
                parent.setFullName(savedUser.getUsername());
            }
            parentRepository.save(parent);
        }

        return savedUser;
    }

    public User modifierUser(User u) {
        if (u.getPassword() != null && !u.getPassword().startsWith("$2a$") && !u.getPassword().startsWith("$2b$") && !u.getPassword().startsWith("$2y$")) {
            u.setPassword(passwordEncoder.encode(u.getPassword()));
        }
        return userRepository.save(u);
    }

    @Transactional
    public void supprimerUser(Long id) {
        // 1. Supprimer les patients dont le parent est ce user (enfants du parent)
        List<Patient> children = patientRepository.findByParent_Id(id);
        patientRepository.deleteAll(children);

        // 2. Supprimer le patient qui utilise ce user (si existe)
        if (patientRepository.existsById(id)) {
            patientRepository.deleteById(id);
        }

        // 3. Supprimer le parent (si ce user est un parent)
        if (parentRepository.existsById(id)) {
            parentRepository.deleteById(id);
        }

        // 4. Supprimer le doctor (si ce user est un docteur)
        if (doctorRepository.existsById(id)) {
            doctorRepository.deleteById(id);
        }

        // 5. Supprimer l'admin (si ce user est un admin)
        if (adminRepository.existsById(id)) {
            adminRepository.deleteById(id);
        }

        // 6. Supprimer le user
        userRepository.deleteById(id);
    }

    public List<User> listeUsers() {
        return userRepository.findAll();
    }

    public Page<User> search(String q, Pageable pageable) {
        return userRepository.findAll(SearchSpecifications.userSearchSpec(q), pageable);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

}
