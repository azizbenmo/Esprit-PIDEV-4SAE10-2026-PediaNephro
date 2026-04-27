package esprit.User.controller;

import esprit.User.dto.DoctorRegistrationDto;
import esprit.User.dto.DoctorAnnuaireDto;
import esprit.User.dto.DoctorShowcaseDto;
import esprit.User.dto.EventInscriptionOptionDto;
import esprit.User.dto.PaginatedResponse;
import esprit.User.entities.Doctor;
import esprit.User.pedianephro.PaginationUtils;
import esprit.User.services.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"mic1/doctors", "doctor"})
public class DoctorController {


    @Autowired
    private DoctorService doctorService;

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    ///
    public List<Doctor> getAllDoctors() {
        return doctorService.findAll();
    }
/// /
    /** Page d’accueil : médecins acceptés (sans auth). Doit être déclaré avant GET /{id}. */
    @GetMapping("/public/showcase")
    public List<DoctorShowcaseDto> showcaseForHome() {
        return doctorService.findShowcaseForHome();
    }

    /** Annuaire : médecins issus de la table {@code doctors} (acceptés), sans auth. */
    @GetMapping("/public/annuaire")
    public List<DoctorAnnuaireDto> annuairePublic(
            @RequestParam(value = "specialite", required = false) String specialite) {
        return doctorService.findForAnnuaire(specialite != null ? specialite : "");
    }

    /** Médecins acceptés pour inscription événement (sans JWT). */
    @GetMapping("/public/events-inscription-options")
    public List<EventInscriptionOptionDto> eventsInscriptionOptionsDoctors() {
        return doctorService.findAcceptedDoctorsForEventInscription();
    }

    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public Doctor getDoctor(@PathVariable Long id) {
        return doctorService.findById(id);
    }

    @PostMapping("/register")
    public Doctor registerDoctor(
            @RequestPart("doctor") DoctorRegistrationDto dto,
            @RequestPart("cv") MultipartFile cvFile
    ) throws IOException {
        return doctorService.createDoctorFromDto(dto, cvFile);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public PaginatedResponse<Doctor> searchDoctors(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size
    ) {
        Pageable pageable = PaginationUtils.buildPageable(page, size, Sort.by(Sort.Direction.DESC, "user.createdAt"));
        Page<Doctor> result = doctorService.search(q, pageable);
        return PaginationUtils.fromPage(result);
    }

    @PutMapping("/{id:\\d+}/accept")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public Doctor acceptDoctor(@PathVariable Long id) {
        return doctorService.acceptDoctor(id);
    }

    @PutMapping("/{id:\\d+}/reject")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public Doctor rejectDoctor(@PathVariable Long id) {
        return doctorService.rejectDoctor(id);
    }

    /**
     * Recopie tous les doctors {@code ACCEPTED} vers la table {@code medecin} du microservice Consultation
     * (utile après migration ou si la sync HTTP avait échoué).
     */
    @PostMapping("/consultation-sync")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Integer> syncAllAcceptedToConsultation() {
        int n = doctorService.syncAllAcceptedDoctorsToConsultation();
        return Map.of("acceptedDoctorsSynced", n);
    }

    /**
     * Synchronise un doctor précis (id table doctors/users) vers consultation_db.medecin.
     */
    @PostMapping("/consultation-sync/{id:\\d+}")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> syncOneDoctorToConsultation(@PathVariable Long id) {
        Doctor d = doctorService.syncDoctorToConsultation(id);
        return Map.of(
                "doctorId", d.getId(),
                "status", d.getStatus().name(),
                "synced", true
        );
    }
}
