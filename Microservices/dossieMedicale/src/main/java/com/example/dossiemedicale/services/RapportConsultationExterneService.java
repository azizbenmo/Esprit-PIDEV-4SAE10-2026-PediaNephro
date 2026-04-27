package com.example.dossiemedicale.services;

import com.example.dossiemedicale.DTO.SyncRapportConsultationRequest;
import com.example.dossiemedicale.DTO.RapportConsultationParentResponse;
import com.example.dossiemedicale.entities.DossierMedical;
import com.example.dossiemedicale.entities.RapportConsultationExterne;
import com.example.dossiemedicale.repositoories.DossierMedicalRepository;
import com.example.dossiemedicale.repositoories.RapportConsultationExterneRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class RapportConsultationExterneService {

    private final RapportConsultationExterneRepository rapportRepository;
    private final DossierMedicalRepository dossierMedicalRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${microservice.consultation.url:http://localhost:8089/apiConsultation}")
    private String consultationBaseUrl;

    public RapportConsultationExterne sync(SyncRapportConsultationRequest request) {
        if (request.getConsultationId() == null) {
            throw new IllegalArgumentException("consultationId est obligatoire");
        }
        DossierMedical dossier = resolveDossier(request);

        RapportConsultationExterne rapport = rapportRepository
                .findByConsultationId(request.getConsultationId())
                .orElseGet(RapportConsultationExterne::new);

        rapport.setConsultationId(request.getConsultationId());
        rapport.setPatientUserId(request.getPatientUserId());
        rapport.setMedecinUserId(request.getMedecinUserId());
        rapport.setMedecinPrenom(request.getMedecinPrenom());
        rapport.setMedecinNom(request.getMedecinNom());
        rapport.setEnfantPrenom(request.getEnfantPrenom());
        rapport.setEnfantNom(request.getEnfantNom());
        rapport.setParentPrenom(request.getParentPrenom());
        rapport.setParentNom(request.getParentNom());
        rapport.setSpecialite(request.getSpecialite());
        rapport.setMotif(request.getMotif());
        rapport.setDiagnostic(request.getDiagnostic());
        rapport.setObservations(request.getObservations());
        rapport.setRecommandations(request.getRecommandations());
        rapport.setEtatPatient(request.getEtatPatient());
        rapport.setFichierPath(request.getFichierPath());
        rapport.setDateSouhaitee(request.getDateSouhaitee());
        rapport.setDateConfirmee(request.getDateConfirmee());
        rapport.setDateRapport(request.getDateRapport());
        rapport.setDossier(dossier);

        return rapportRepository.save(rapport);
    }

    public List<RapportConsultationParentResponse> getRapportsPourParentConnecte(Long dossierId, String authorizationHeader) {
        if (dossierId == null || dossierId <= 0) {
            throw new IllegalArgumentException("id dossier invalide");
        }

        DossierMedical dossier = dossierMedicalRepository.findById(dossierId)
                .orElseThrow(() -> new IllegalArgumentException("Dossier medical introuvable (id=" + dossierId + ")"));

        Map<String, Object> claims = decodeClaims(authorizationHeader);
        String role = claims.get("role") != null ? String.valueOf(claims.get("role")) : "";
        Long userId = extractUserId(claims);

        if (userId == null) {
            throw new IllegalArgumentException("Token invalide: userId manquant");
        }
        String roleUpper = role.toUpperCase();
        if (!role.isBlank() && !roleUpper.contains("PARENT") && !roleUpper.contains("PATIENT")) {
            throw new IllegalArgumentException("Acces reserve au parent connecte");
        }

        Long dossierParentUserId = dossier.getEnfant() != null
                && dossier.getEnfant().getPatient() != null
                ? dossier.getEnfant().getPatient().getUserId()
                : null;

        if (dossierParentUserId == null || !dossierParentUserId.equals(userId)) {
            throw new IllegalArgumentException("Acces refuse a ce dossier");
        }

        String parentPrenom = dossier.getEnfant() != null && dossier.getEnfant().getPatient() != null
                ? dossier.getEnfant().getPatient().getPrenom()
                : null;
        String parentNom = dossier.getEnfant() != null && dossier.getEnfant().getPatient() != null
                ? dossier.getEnfant().getPatient().getNom()
                : null;
        String enfantPrenom = dossier.getEnfant() != null ? dossier.getEnfant().getPrenom() : null;
        String enfantNom = dossier.getEnfant() != null ? dossier.getEnfant().getNom() : null;

        try {
            List<RapportConsultationParentResponse> rapportsConsultation = fetchRapportsDepuisConsultation(dossier);
            repairRapportsExternes(dossier, rapportsConsultation);
            return rapportsConsultation;
        } catch (Exception ex) {
            return rapportRepository.findByDossier_IdDossierOrderByDateRapportDescIdDesc(dossierId)
                    .stream()
                    .map(rapport -> RapportConsultationParentResponse.builder()
                            .id(rapport.getId())
                            .consultationId(rapport.getConsultationId())
                            .diagnostic(rapport.getDiagnostic())
                            .observations(rapport.getObservations())
                            .recommandations(rapport.getRecommandations())
                            .etatPatient(rapport.getEtatPatient())
                            .dateRapport(rapport.getDateRapport())
                            .fichierPath(rapport.getFichierPath())
                            .enfantPrenom(rapport.getEnfantPrenom() != null ? rapport.getEnfantPrenom() : enfantPrenom)
                            .enfantNom(rapport.getEnfantNom() != null ? rapport.getEnfantNom() : enfantNom)
                            .parentPrenom(rapport.getParentPrenom() != null ? rapport.getParentPrenom() : parentPrenom)
                            .parentNom(rapport.getParentNom() != null ? rapport.getParentNom() : parentNom)
                            .medecinPrenom(rapport.getMedecinPrenom())
                            .medecinNom(rapport.getMedecinNom())
                            .specialite(rapport.getSpecialite())
                            .motif(rapport.getMotif())
                            .build())
                    .toList();
        }
    }

    private DossierMedical resolveDossier(SyncRapportConsultationRequest request) {
        if (request.getDossierMedicalId() != null && request.getDossierMedicalId() > 0) {
            return dossierMedicalRepository.findById(request.getDossierMedicalId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Aucun dossier medical trouve pour dossierMedicalId=" + request.getDossierMedicalId()));
        }
        if (request.getPatientUserId() == null) {
            throw new IllegalArgumentException("patientUserId est obligatoire");
        }
        return dossierMedicalRepository
                .findFirstByEnfant_UserPatientId(request.getPatientUserId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Aucun dossier medical trouve pour patientUserId=" + request.getPatientUserId()));
    }

    private List<RapportConsultationParentResponse> fetchRapportsDepuisConsultation(DossierMedical dossier) {
        Long dossierId = dossier.getIdDossier();
        RapportConsultationParentResponse[] response = restTemplate.getForObject(
                consultationBaseUrl + "/internal/rapports-consultation/dossier/" + dossierId,
                RapportConsultationParentResponse[].class
        );
        if (response != null && response.length > 0) {
            return List.of(response);
        }
        Long patientUserId = dossier.getEnfant() != null ? dossier.getEnfant().getUserPatientId() : null;
        if (patientUserId != null && patientUserId > 0) {
            RapportConsultationParentResponse[] byPatient = restTemplate.getForObject(
                    consultationBaseUrl + "/internal/rapports-consultation/patient-user/" + patientUserId,
                    RapportConsultationParentResponse[].class
            );
            if (byPatient != null && byPatient.length > 0) {
                return List.of(byPatient);
            }
        }
        return Collections.emptyList();
    }

    private void repairRapportsExternes(DossierMedical dossier, List<RapportConsultationParentResponse> rapports) {
        List<RapportConsultationExterne> existants = rapportRepository
                .findByDossier_IdDossierOrderByDateRapportDescIdDesc(dossier.getIdDossier());
        Set<Long> consultationIds = rapports.stream()
                .map(RapportConsultationParentResponse::getConsultationId)
                .filter(id -> id != null && id > 0)
                .collect(java.util.stream.Collectors.toSet());

        List<RapportConsultationExterne> stale = existants.stream()
                .filter(rapport -> rapport.getConsultationId() != null && !consultationIds.contains(rapport.getConsultationId()))
                .toList();
        if (!stale.isEmpty()) {
            rapportRepository.deleteAll(stale);
        }

        for (RapportConsultationParentResponse rapportLu : rapports) {
            if (rapportLu.getConsultationId() == null) {
                continue;
            }
            RapportConsultationExterne rapport = rapportRepository
                    .findByConsultationId(rapportLu.getConsultationId())
                    .orElseGet(RapportConsultationExterne::new);

            rapport.setConsultationId(rapportLu.getConsultationId());
            rapport.setPatientUserId(dossier.getEnfant() != null ? dossier.getEnfant().getUserPatientId() : null);
            rapport.setDiagnostic(rapportLu.getDiagnostic());
            rapport.setObservations(rapportLu.getObservations());
            rapport.setRecommandations(rapportLu.getRecommandations());
            rapport.setEtatPatient(rapportLu.getEtatPatient());
            rapport.setDateRapport(rapportLu.getDateRapport());
            rapport.setFichierPath(rapportLu.getFichierPath());
            rapport.setEnfantPrenom(rapportLu.getEnfantPrenom());
            rapport.setEnfantNom(rapportLu.getEnfantNom());
            rapport.setParentPrenom(rapportLu.getParentPrenom());
            rapport.setParentNom(rapportLu.getParentNom());
            rapport.setMedecinPrenom(rapportLu.getMedecinPrenom());
            rapport.setMedecinNom(rapportLu.getMedecinNom());
            rapport.setSpecialite(rapportLu.getSpecialite());
            rapport.setMotif(rapportLu.getMotif());
            rapport.setDossier(dossier);
            rapportRepository.save(rapport);
        }
    }

    private Map<String, Object> decodeClaims(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Header Authorization Bearer manquant");
        }
        String token = authorizationHeader.substring(7).trim();
        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Token JWT invalide");
        }

        try {
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            return objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception ex) {
            throw new IllegalArgumentException("Impossible de decoder le token JWT");
        }
    }

    private Long extractUserId(Map<String, Object> claims) {
        Object raw = claims.get("id");
        if (raw == null) {
            raw = claims.get("userId");
        }
        if (raw == null) {
            return null;
        }
        if (raw instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(raw));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
