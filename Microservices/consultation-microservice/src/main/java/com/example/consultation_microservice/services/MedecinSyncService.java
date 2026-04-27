package com.example.consultation_microservice.services;

import com.example.consultation_microservice.dto.MedecinSyncRequest;
import com.example.consultation_microservice.entities.Medecin;
import com.example.consultation_microservice.repositories.MedecinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class MedecinSyncService {

    /** Aligné sur le filtre Angular « Demande de consultation ». */
    private static final String DEFAULT_SPECIALITE = "Néphrologie Pédiatrique";

    @Autowired
    private MedecinRepository medecinRepository;

    @Transactional
    public Medecin upsertFromUser(MedecinSyncRequest req) {
        if (req.getUserId() == null) {
            throw new IllegalArgumentException("userId obligatoire");
        }
        if (Boolean.FALSE.equals(req.getDisponible())) {
            Optional<Medecin> opt = medecinRepository.findByUserId(req.getUserId());
            if (opt.isEmpty()) {
                return null;
            }
            Medecin m = opt.get();
            m.setDisponible(false);
            return medecinRepository.save(m);
        }

        Medecin m = medecinRepository.findByUserId(req.getUserId())
                .orElseGet(() -> medecinRepository.findById(req.getUserId())
                        // Répare les anciennes lignes seed/non liées (userId NULL) quand la PK correspond au userId.
                        .filter(existing -> existing.getUserId() == null)
                        // Sinon, réparer une ligne orpheline retrouvée par email.
                        .or(() -> findOrphanByEmail(req.getEmail()))
                        .orElseGet(Medecin::new));
        m.setUserId(req.getUserId());
        m.setEmail(req.getEmail() != null ? req.getEmail().trim() : null);
        applyFullName(m, req.getFullName());
        m.setSpecialite(normalizeSpecialite(req.getSpecialite()));
        m.setTelephone(req.getTelephone() != null ? req.getTelephone().trim() : null);
        m.setAnneesExperience(req.getAnneesExperience());
        m.setDisponible(req.getDisponible() == null || Boolean.TRUE.equals(req.getDisponible()));
        m.setVille(req.getVille() != null && !req.getVille().isBlank() ? req.getVille().trim() : null);
        if (m.getAdresseCabinet() == null) {
            m.setAdresseCabinet("");
        }
        return medecinRepository.save(m);
    }

    private Optional<Medecin> findOrphanByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return medecinRepository.findFirstByEmailIgnoreCase(email.trim())
                .filter(existing -> existing.getUserId() == null);
    }

    static void applyFullName(Medecin m, String fullName) {
        String t = fullName == null ? "" : fullName.trim().replaceFirst("(?i)^dr\\.?\\s*", "").trim();
        if (t.isEmpty()) {
            m.setPrenom("Médecin");
            m.setNom("");
            return;
        }
        String[] parts = t.split("\\s+", 2);
        m.setPrenom(parts[0]);
        m.setNom(parts.length > 1 ? parts[1] : "");
    }

    static String normalizeSpecialite(String s) {
        if (s == null || s.isBlank()) {
            return DEFAULT_SPECIALITE;
        }
        String lower = s.toLowerCase();
        if (lower.contains("néphro") || lower.contains("nephro")) {
            return DEFAULT_SPECIALITE;
        }
        return s.trim();
    }
}
