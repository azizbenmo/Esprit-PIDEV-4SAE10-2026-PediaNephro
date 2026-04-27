package esprit.User.controller;

import esprit.User.dto.*;
import esprit.User.entities.Patient;
import esprit.User.services.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("mic1/patients")
public class PatientController {


    @Autowired
    private PatientService patientService;

    @PostMapping("/register")
    public Patient createPatient(@RequestBody PatientRegistrationDto dto) {
        return patientService.createPatientWithUser(dto);
    }

    @PutMapping("/{id:\\d+}")
    public Patient updatePatient(@PathVariable Long id, @RequestBody PatientUpdateDto dto) {
        return patientService.updatePatientWithUser(id, dto);
    }

    @GetMapping
    public List<DoctorPatientResponseDto> getAllPatients(
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) Long doctorId
    ) {
        if (parentId != null && doctorId != null) {
            throw new RuntimeException("Utilisez soit parentId soit doctorId, pas les deux");
        }
        if (parentId != null) {
            return patientService.findByParentIdDto(parentId);
        }
        if (doctorId != null) {
            return patientService.findByDoctorIdDto(doctorId);
        }
        return patientService.findAllDto();
    }

    /** Patients pour inscription événement (sans JWT). */
    @GetMapping("/public/events-inscription-options")
    public List<EventInscriptionOptionDto> eventsInscriptionOptionsPatients() {
        return patientService.findAllPatientsForEventInscription();
    }

    @GetMapping("/me")
    public List<DoctorPatientResponseDto> getMyPatients(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new RuntimeException("Utilisateur non authentifie");
        }
        return patientService.findMyPatientsDto(authentication.getName());
    }

    /** Patients du médecin connecté pour inscription aux événements (JWT). */
    @GetMapping("/me/events-inscription-options")
    public List<EventInscriptionOptionDto> getMyPatientsEventsInscriptionOptions(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new RuntimeException("Utilisateur non authentifie");
        }
        return patientService.findMyPatientsForEventInscription(authentication.getName());
    }

    /**
     * Enfants du parent connecté. Chemin à plusieurs segments pour éviter que Spring ne prenne
     * {@code my-children} pour {@code /{id}} (Long) → 400 Bad Request.
     */
    @GetMapping("/me/linked-children")
    public List<DoctorPatientResponseDto> getMyChildren(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return List.of();
        }
        return patientService.findMyChildrenForCurrentParent(authentication.getName());
    }

    @GetMapping("/{id:\\d+}")
    public Patient getPatient(@PathVariable Long id) {
        return patientService.findById(id);
    }

    @DeleteMapping("/{id:\\d+}")
    public void deletePatient(@PathVariable Long id) {
        patientService.delete(id);
    }

    /**
     * Endpoint interne appelé par le microservice DossierMedical
     * pour synchroniser un enfant créé là-bas vers la table patients.
     */
    @PostMapping("/sync-from-dossier")
    public ResponseEntity<PatientSyncResponseDto> syncPatientFromDossier(
            @RequestBody PatientSyncRequest dto) {

        Patient p = patientService.createOrFindSyncedPatient(dto);
        return ResponseEntity.ok(new PatientSyncResponseDto(p.getId(), p.getFullName()));
    }
}

