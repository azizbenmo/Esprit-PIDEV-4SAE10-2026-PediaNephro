package esprit.User.services;

import esprit.User.dto.ConsultationMedecinSyncDto;
import esprit.User.entities.Doctor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Crée ou met à jour une fiche {@code medecin} dans {@code consultation_db} quand un doctor est accepté.
 */
@Service
public class ConsultationMedecinSyncClient {

    private static final Logger log = LoggerFactory.getLogger(ConsultationMedecinSyncClient.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final String syncUrl;

    public ConsultationMedecinSyncClient(
            @Value("${consultation.service.sync-url:http://localhost:8080/apiConsultation/internal/medecin/sync}") String syncUrl) {
        this.syncUrl = syncUrl;
    }

    public void syncAcceptedDoctor(Doctor doctor) {
        if (doctor == null || doctor.getUser() == null) {
            log.warn("[Consultation sync] doctor ou user null, ignoré");
            return;
        }
        ConsultationMedecinSyncDto dto = new ConsultationMedecinSyncDto();
        dto.setUserId(doctor.getUser().getId());
        dto.setEmail(doctor.getUser().getEmail());
        dto.setFullName(doctor.getFullName());
        dto.setSpecialite(doctor.getSpeciality());
        dto.setTelephone(doctor.getPhone());
        dto.setAnneesExperience(doctor.getYearsOfExperience());
        dto.setDisponible(true);
        dto.setVille(doctor.getHospital());
        post(dto);
    }

    public void markMedecinUnavailable(Long userId) {
        if (userId == null) {
            return;
        }
        ConsultationMedecinSyncDto dto = new ConsultationMedecinSyncDto();
        dto.setUserId(userId);
        dto.setDisponible(false);
        post(dto);
    }

    private void post(ConsultationMedecinSyncDto dto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            ResponseEntity<Object> res = restTemplate.postForEntity(
                    syncUrl, new HttpEntity<>(dto, headers), Object.class);
            if (!res.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("HTTP " + res.getStatusCode().value());
            }
            log.info("[Consultation sync] OK userId={} disponible={}", dto.getUserId(), dto.getDisponible());
        } catch (Exception ex) {
            log.error("[Consultation sync] échec userId={}: {}", dto.getUserId(), ex.getMessage());
            throw new RuntimeException("Échec de synchronisation consultation pour userId=" + dto.getUserId(), ex);
        }
    }
}
