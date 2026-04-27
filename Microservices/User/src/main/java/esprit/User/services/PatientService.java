package esprit.User.services;

import esprit.User.dto.*;
import esprit.User.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import esprit.User.repositories.*;
import java.util.UUID;


import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public Patient createPatientWithUser(PatientRegistrationDto dto) {
        // Validation du parentId (obligatoire)
        if (dto.getParentId() == null) {
            throw new RuntimeException("parentId est obligatoire pour créer un patient");
        }

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
        user.setRole(Role.PATIENT);
        user = userRepository.save(user);

        // 2) récupérer le parent (obligatoire)
        Parent parent = parentRepository.findById(dto.getParentId())
                .orElseThrow(() -> new RuntimeException("Parent introuvable avec id=" + dto.getParentId()));

        // 3) récupérer le docteur (optionnel)
        Doctor doctor = null;
        if (dto.getDoctorId() != null) {
            doctor = doctorRepository.findById(dto.getDoctorId())
                    .orElseThrow(() -> new RuntimeException("Doctor introuvable avec id=" + dto.getDoctorId()));
        }

        // 4) créer le Patient
        Patient patient = new Patient();
        patient.setFullName(dto.getFullName());
        patient.setBirthDate(dto.getBirthDate());
        patient.setGender(Gender.valueOf(dto.getGender()));
        patient.setParent(parent);
        patient.setDoctor(doctor);
        patient.setUser(user);

        return patientRepository.save(patient);
    }

    @Transactional
    public Patient updatePatientWithUser(Long id, PatientUpdateDto dto) {
        // Récupérer le patient existant
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient introuvable avec id=" + id));

        // Récupérer le user associé
        User user = patient.getUser();
        if (user == null) {
            throw new RuntimeException("User associé introuvable pour le patient id=" + id);
        }

        // Mettre à jour les champs User si fournis
        if (dto.getUsername() != null && !dto.getUsername().isEmpty()) {
            User existingUser = userRepository.findByUsername(dto.getUsername());
            if (existingUser != null && !existingUser.getId().equals(user.getId())) {
                throw new RuntimeException("Un utilisateur avec ce username existe déjà: " + dto.getUsername());
            }
            user.setUsername(dto.getUsername());
        }

        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
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

        userRepository.save(user);

        // Mettre à jour les champs Patient si fournis
        if (dto.getFullName() != null && !dto.getFullName().isEmpty()) {
            patient.setFullName(dto.getFullName());
        }

        if (dto.getBirthDate() != null) {
            patient.setBirthDate(dto.getBirthDate());
        }

        if (dto.getGender() != null) {
            patient.setGender(dto.getGender());
        }

        // Mettre à jour les relations si fournies
        if (dto.getParentId() != null) {
            Parent parent = parentRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent introuvable avec id=" + dto.getParentId()));
            patient.setParent(parent);
        }

        if (dto.getDoctorId() != null) {
            Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                    .orElseThrow(() -> new RuntimeException("Doctor introuvable avec id=" + dto.getDoctorId()));
            patient.setDoctor(doctor);
        } else if (dto.getDoctorId() == null && patient.getDoctor() != null) {
            // Si doctorId est explicitement null, on peut vouloir retirer le docteur
            // Mais ici on ne fait rien pour éviter de perdre l'info si ce n'est pas intentionnel
        }

        return patientRepository.save(patient);
    }

    public List<Patient> findAll() {
        return patientRepository.findAll();
    }

    public List<DoctorPatientResponseDto> findAllDto() {
        return toDoctorPatientDtos(patientRepository.findAll());
    }

    /**
     * Tous les patients (comptes User) pour les listes d’inscription aux événements (id = compte User).
     */
    @Transactional(readOnly = true)
    public List<EventInscriptionOptionDto> findAllPatientsForEventInscription() {
        return patientRepository.findAll().stream()
                .map(p -> {
                    User u = p.getUser();
                    String email = u != null && u.getEmail() != null ? u.getEmail() : "";
                    String name = p.getFullName() != null && !p.getFullName().isBlank() ? p.getFullName() : "Patient";
                    return new EventInscriptionOptionDto(p.getId(), name, email);
                })
                .toList();
    }

    public List<DoctorPatientResponseDto> findByParentIdDto(Long parentId) {
        return toDoctorPatientDtos(patientRepository.findByParent_Id(parentId));
    }

    /**
     * Enfants du parent connecté (JWT), sans paramètre — évite un mauvais {@code parentId} côté client.
     */
    public List<DoctorPatientResponseDto> findMyChildrenForCurrentParent(String usernameOrEmail) {
        if (usernameOrEmail == null || usernameOrEmail.isBlank()) {
            return List.of();
        }
        User user = userRepository.findFirstByUsernameOrEmail(usernameOrEmail).orElse(null);
        if (user == null || user.getRole() != Role.PARENT) {
            return List.of();
        }
        Parent parent = parentRepository.findByUser_Id(user.getId()).orElse(null);
        if (parent == null) {
            return List.of();
        }
        return findByParentIdDto(parent.getId());
    }

    public List<DoctorPatientResponseDto> findByDoctorIdDto(Long doctorId) {
        return toDoctorPatientDtos(patientRepository.findByDoctor_Id(doctorId));
    }

    public List<Patient> findMyPatients(String username) {
        if (username == null || username.isBlank()) {
            throw new RuntimeException("Username du docteur connecte invalide");
        }

        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("Utilisateur connecte introuvable");
        }

        if (user.getRole() != Role.DOCTOR) {
            throw new RuntimeException("Acces refuse: l'utilisateur connecte n'est pas DOCTOR");
        }

        Doctor doctor = doctorRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new RuntimeException("Profil doctor introuvable"));

        return patientRepository.findByDoctor_Id(doctor.getId());
    }

    public List<DoctorPatientResponseDto> findMyPatientsDto(String username) {
        return toDoctorPatientDtos(findMyPatients(username));
    }

    /**
     * Patients du médecin connecté (JWT), format inscription événements ({@code userId} = compte User).
     */
    @Transactional(readOnly = true)
    public List<EventInscriptionOptionDto> findMyPatientsForEventInscription(String username) {
        return findMyPatients(username).stream()
                .map(p -> {
                    User u = p.getUser();
                    Long userId = u != null ? u.getId() : p.getId();
                    String email = u != null && u.getEmail() != null ? u.getEmail() : "";
                    String name = p.getFullName() != null && !p.getFullName().isBlank() ? p.getFullName() : "Patient";
                    return new EventInscriptionOptionDto(userId, name, email);
                })
                .toList();
    }

    private List<DoctorPatientResponseDto> toDoctorPatientDtos(List<Patient> patients) {
        return patients.stream()
                .map(patient -> new DoctorPatientResponseDto(
                        patient.getId(),
                        patient.getFullName(),
                        patient.getBirthDate(),
                        patient.getGender() == null ? null : patient.getGender().name(),
                        patient.getParent() == null ? null : patient.getParent().getId(),
                        patient.getParent() == null ? null : patient.getParent().getFullName()
                ))
                .collect(Collectors.toList());
    }

    public Patient findById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient introuvable avec id=" + id));
    }

    @Transactional
    public void delete(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient introuvable avec id=" + id));

        User user = patient.getUser();
        if (user != null) {
            patientRepository.deleteById(id);
            userRepository.deleteById(user.getId());
        } else {
            patientRepository.deleteById(id);
        }
    }

    public Patient createOrFindSyncedPatient(PatientSyncRequest dto) {
        if (dto.getFullName() == null || dto.getFullName().isBlank()) {
            throw new RuntimeException("fullName obligatoire pour la synchronisation patient");
        }

        Parent parent = parentRepository.findById(dto.getParentId())
                .orElseThrow(() -> new RuntimeException("Parent introuvable id=" + dto.getParentId()));

        String normalized = dto.getFullName().trim().replaceAll("\\s+", " ");
        for (Patient existing : patientRepository.findByParent_Id(parent.getId())) {
            if (existing.getFullName() == null || existing.getFullName().isBlank()) {
                continue;
            }
            String exNorm = existing.getFullName().trim().replaceAll("\\s+", " ");
            if (exNorm.equalsIgnoreCase(normalized)) {
                return existing;
            }
        }

        // Créer un User minimal pour cet enfant (pas de login réel)
        User userEnfant = new User();
        userEnfant.setUsername(dto.getFullName().replaceAll("\\s+", ".").toLowerCase()
                + "." + System.currentTimeMillis());
        userEnfant.setEmail("enfant-" + System.currentTimeMillis() + "@pedianephro.local");
        userEnfant.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        userEnfant.setRole(Role.PATIENT);
        userEnfant.setActive(true);
        userEnfant.setEnabled(true);
        userEnfant = userRepository.save(userEnfant);

        // Créer le Patient lié
        Patient patient = new Patient();
        patient.setFullName(dto.getFullName());
        patient.setBirthDate(dto.getBirthDate());
        patient.setGender(dto.getGender() != null && !dto.getGender().isBlank()
                ? Gender.valueOf(dto.getGender().toUpperCase())
                : null);
        patient.setParent(parent);
        patient.setUser(userEnfant);
        // patient.setId sera auto via @MapsId

        return patientRepository.save(patient);
    }
}


