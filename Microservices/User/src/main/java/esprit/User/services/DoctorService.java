package esprit.User.services;

import esprit.User.entities.Doctor;
import esprit.User.entities.DoctorStatus;
import esprit.User.entities.Role;
import esprit.User.entities.User;
import esprit.User.dto.DoctorRegistrationDto;
import esprit.User.dto.DoctorAnnuaireDto;
import esprit.User.dto.DoctorShowcaseDto;
import esprit.User.dto.EventInscriptionOptionDto;
import esprit.User.pedianephro.SearchSpecifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import esprit.User.repositories.DoctorRepository;
import esprit.User.repositories.UserRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

@Service
public class DoctorService {

    private static final Logger log = LoggerFactory.getLogger(DoctorService.class);

    private static final String UPLOAD_DIR = "uploads/cv/";

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Autowired
    private KeycloakService keycloakService;

    @Autowired
    private ConsultationMedecinSyncClient consultationMedecinSyncClient;

    /**
     * Création d’un doctor avec upload CV
     */
    public List<Doctor> findAll() {
        return doctorRepository.findAll();
    }

    /**
     * Médecins acceptés pour les listes d’inscription aux événements (id = compte User).
     */
    @Transactional(readOnly = true)
    public List<EventInscriptionOptionDto> findAcceptedDoctorsForEventInscription() {
        return doctorRepository.findByStatusOrderByYearsOfExperienceDesc(DoctorStatus.ACCEPTED).stream()
                .map(d -> {
                    User u = d.getUser();
                    String email = u != null && u.getEmail() != null ? u.getEmail() : "";
                    String name = d.getFullName() != null && !d.getFullName().isBlank() ? d.getFullName() : "Médecin";
                    return new EventInscriptionOptionDto(d.getId(), name, email);
                })
                .toList();
    }

    public Doctor findById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor introuvable avec id=" + id));
    }

    public Doctor createDoctorFromDto(DoctorRegistrationDto dto, MultipartFile cvFile) throws IOException {
        if (userRepository.findByEmail(dto.getEmail()) != null) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà: " + dto.getEmail());
        }
        User user = new User();
        user.setUsername(dto.getEmail());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.DOCTOR);
        user.setEnabled(false);
        user = userRepository.save(user);

        // Synchroniser avec Keycloak (non-bloquant)
        final String rawPassword = dto.getPassword();
        try {
            keycloakService.createUserInKeycloak(dto.getEmail(), dto.getEmail(), rawPassword, Role.DOCTOR);
        } catch (Exception ex) {
            log.warn("[Keycloak] Sync doctor '{}' échouée (non bloquant): {}", dto.getEmail(), ex.getMessage());
        }

        Doctor doctor = new Doctor();
        doctor.setFullName(dto.getFullName());
        doctor.setSpeciality(dto.getSpecialty());
        doctor.setPhone(dto.getPhone());
        doctor.setLicenseNumber(dto.getLicenseNumber());
        doctor.setYearsOfExperience(dto.getYearsOfExperience());
        doctor.setUser(user);

        Doctor savedDoctor = createDoctor(user, doctor, cvFile);
        emailVerificationService.createAndSendVerificationToken(user);
        return savedDoctor;
    }

    public Doctor createDoctor(User user,
                               Doctor doctor,
                               MultipartFile cvFile) throws IOException {

        // 1️⃣ Vérification du CV
        if (cvFile == null || cvFile.isEmpty()) {
            throw new RuntimeException("CV obligatoire");
        }

        if (!cvFile.getContentType().equals("application/pdf")) {
            throw new RuntimeException("Seuls les fichiers PDF sont acceptés");
        }

        // 2️⃣ Création dossier s'il n'existe pas
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 3️⃣ Nom du fichier
        String fileName = "doctor_" + user.getId() + ".pdf";
        Path filePath = Paths.get(UPLOAD_DIR + fileName);

        // 4️⃣ Sauvegarde du fichier
        Files.write(filePath, cvFile.getBytes());

        // 5️⃣ Liaison Doctor ↔ User
        doctor.setUser(user);
        doctor.setCvPath(filePath.toString());

        // 6️⃣ Valeurs par défaut
        doctor.setStatus(DoctorStatus.PENDING);
        doctor.setAiScore(0); // sera calculé plus tard par l’IA

        return doctorRepository.save(doctor);
    }

    /**
     * Mise à jour du score IA
     */
    public Doctor updateAiScore(Long doctorId, Integer score) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor introuvable"));

        doctor.setAiScore(score);
        return doctorRepository.save(doctor);
    }

    /**
     * Changer le statut (Admin)
     */
    @Transactional
    public Doctor updateStatus(Long doctorId, DoctorStatus status) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor introuvable"));

        doctor.setStatus(status);
        Doctor saved = doctorRepository.save(doctor);
        applyConsultationSyncForStatus(saved);
        return saved;
    }

    private static final int MIN_EXPERIENCE_YEARS = 5;

    /**
     * Accepter un docteur (admin). Nécessite plus de 5 ans d'expérience.
     */
    @Transactional
    public Doctor acceptDoctor(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor introuvable avec id=" + doctorId));

        Integer years = doctor.getYearsOfExperience();
        if (years == null || years <= MIN_EXPERIENCE_YEARS) {
            throw new RuntimeException("Expérience insuffisante : le docteur doit avoir plus de " + MIN_EXPERIENCE_YEARS + " ans d'expérience (actuellement : " + (years != null ? years : 0) + ")");
        }

        doctor.setStatus(DoctorStatus.ACCEPTED);
        if (doctor.getUser() != null) {
            doctor.getUser().setEnabled(true);
            userRepository.save(doctor.getUser());
        }
        Doctor saved = doctorRepository.save(doctor);
        consultationMedecinSyncClient.syncAcceptedDoctor(saved);
        return saved;
    }

    /**
     * Refuser un docteur (admin).
     */
    public Doctor rejectDoctor(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor introuvable avec id=" + doctorId));

        doctor.setStatus(DoctorStatus.REJECTED);
        Doctor saved = doctorRepository.save(doctor);
        if (saved.getUser() != null) {
            consultationMedecinSyncClient.markMedecinUnavailable(saved.getUser().getId());
        }
        return saved;
    }

    /**
     * Remplit {@code consultation_db.medecin} pour tous les doctors déjà acceptés (migration / réparation).
     *
     * @return nombre de tentatives envoyées (succès logué côté client HTTP)
     */
    public int syncAllAcceptedDoctorsToConsultation() {
        List<Doctor> doctors = doctorRepository.findByStatusOrderByYearsOfExperienceDesc(DoctorStatus.ACCEPTED);
        int n = 0;
        for (Doctor d : doctors) {
            consultationMedecinSyncClient.syncAcceptedDoctor(d);
            n++;
        }
        return n;
    }

    /**
     * Force la synchronisation d'un docteur précis vers consultation_db.
     */
    public Doctor syncDoctorToConsultation(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor introuvable avec id=" + doctorId));
        if (doctor.getStatus() != DoctorStatus.ACCEPTED) {
            throw new RuntimeException("Seuls les doctors ACCEPTED peuvent être synchronisés vers consultation.");
        }
        consultationMedecinSyncClient.syncAcceptedDoctor(doctor);
        return doctor;
    }

    /**
     * Répare automatiquement les médecins déjà ACCEPTED au démarrage (cas sync HTTP ratée par le passé).
     */
    @EventListener(ApplicationReadyEvent.class)
    public void syncAcceptedDoctorsOnStartup() {
        try {
            int n = syncAllAcceptedDoctorsToConsultation();
            log.info("[Consultation sync] startup repair done, doctors sent={}", n);
        } catch (Exception ex) {
            log.warn("[Consultation sync] startup repair failed (non bloquant): {}", ex.getMessage());
        }
    }

    private void applyConsultationSyncForStatus(Doctor saved) {
        if (saved.getStatus() == DoctorStatus.ACCEPTED) {
            consultationMedecinSyncClient.syncAcceptedDoctor(saved);
        } else if (saved.getStatus() == DoctorStatus.REJECTED && saved.getUser() != null) {
            consultationMedecinSyncClient.markMedecinUnavailable(saved.getUser().getId());
        }
    }

    public Page<Doctor> search(String q, Pageable pageable) {
        return doctorRepository.findAll(SearchSpecifications.doctorSearchSpec(q), pageable);
    }

    /** Médecins acceptés pour la page d’accueil (public, sans données sensibles). */
    public List<DoctorShowcaseDto> findShowcaseForHome() {
        Pageable pageable = PageRequest.of(0, 12, Sort.by(Sort.Direction.DESC, "yearsOfExperience"));
        return doctorRepository.findByStatus(DoctorStatus.ACCEPTED, pageable).getContent().stream()
                .map(this::toShowcaseDto)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Annuaire patient : tous les médecins acceptés, filtrés par libellé de spécialité (contient, insensible à la casse).
     */
    public List<DoctorAnnuaireDto> findForAnnuaire(String specialiteFiltre) {
        // Assure la liaison User.doctor -> Consultation.medecin avant d'exposer l'annuaire.
        try {
            syncAllAcceptedDoctorsToConsultation();
        } catch (Exception ex) {
            log.warn("[Consultation sync] annuaire pre-sync failed (non bloquant): {}", ex.getMessage());
        }
        String needle = specialiteFiltre != null ? specialiteFiltre.trim().toLowerCase() : "";
        return doctorRepository.findByStatusOrderByYearsOfExperienceDesc(DoctorStatus.ACCEPTED).stream()
                .filter(d -> specialiteMatches(d, needle))
                .map(this::toAnnuaireDto)
                .filter(Objects::nonNull)
                .toList();
    }

    private boolean specialiteMatches(Doctor d, String needleLower) {
        if (needleLower.isEmpty()) {
            return true;
        }
        String spec = d.getSpeciality() != null ? d.getSpeciality().toLowerCase() : "";
        return spec.contains(needleLower) || needleLower.contains(spec);
    }

    private DoctorShowcaseDto toShowcaseDto(Doctor d) {
        if (d == null) {
            return null;
        }
        int years = d.getYearsOfExperience() != null ? d.getYearsOfExperience() : 0;
        double rating = publicRatingFromDoctor(d);
        String spec = defaultSpecialtyLabel(d);
        String tagline = (d.getHospital() != null && !d.getHospital().isBlank())
                ? d.getHospital()
                : "Membre du réseau PédiaNéphro";
        DoctorShowcaseDto dto = new DoctorShowcaseDto();
        dto.setId(d.getId());
        dto.setFullName(d.getFullName() != null ? d.getFullName() : "Médecin");
        dto.setSpecialty(spec);
        dto.setYearsOfExperience(years);
        dto.setRating(rating);
        dto.setTagline(tagline);
        return dto;
    }

    private DoctorAnnuaireDto toAnnuaireDto(Doctor d) {
        if (d == null) {
            return null;
        }
        int years = d.getYearsOfExperience() != null ? d.getYearsOfExperience() : 0;
        DoctorAnnuaireDto dto = new DoctorAnnuaireDto();
        dto.setId(d.getId());
        dto.setFullName(d.getFullName() != null ? d.getFullName() : "Médecin");
        dto.setSpecialty(defaultSpecialtyLabel(d));
        dto.setYearsOfExperience(years);
        dto.setRating(publicRatingFromDoctor(d));
        dto.setHospital(d.getHospital());
        return dto;
    }

    private static String defaultSpecialtyLabel(Doctor d) {
        return d.getSpeciality() != null && !d.getSpeciality().isBlank()
                ? d.getSpeciality()
                : "Néphrologie pédiatrique";
    }

    private static double publicRatingFromDoctor(Doctor d) {
        double rating = 4.5;
        if (d.getAiScore() != null && d.getAiScore() > 0) {
            rating = Math.min(5.0, Math.max(3.0, d.getAiScore() / 25.0));
        }
        return rating;
    }
}
