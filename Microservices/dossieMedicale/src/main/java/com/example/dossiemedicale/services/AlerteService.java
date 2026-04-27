package com.example.dossiemedicale.services;

import com.example.dossiemedicale.DTO.AlerteRequest;
import com.example.dossiemedicale.entities.Alerte;
import com.example.dossiemedicale.entities.ConstanteVitale;
import com.example.dossiemedicale.repositoories.AlerteRepository;
import com.example.dossiemedicale.repositoories.ConstanteVitaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlerteService {

    private final AlerteRepository alerteRepository;
    private final ConstanteVitaleRepository constanteVitaleRepository;

    public Alerte ajouterAlerte(AlerteRequest request) {
        log.info("Ajout alerte: niveau={}, message={}, constanteId={}", 
                request.getNiveau(), request.getMessage(), request.getConstanteId());
        
        Alerte alerte = new Alerte();
        alerte.setNiveau(request.getNiveau());
        alerte.setMessage(request.getMessage());
        alerte.setDateDeclenchement(request.getDateDeclenchement());
        
        if (request.getConstanteId() != null) {
            log.info("Cherche constante avec ID: {}", request.getConstanteId());
            ConstanteVitale constante = constanteVitaleRepository.findById(request.getConstanteId())
                    .orElseThrow(() -> {
                        log.error("Constante non trouvée avec ID: {}", request.getConstanteId());
                        return new IllegalArgumentException(
                            "Constante introuvable (id=" + request.getConstanteId() + ")"
                        );
                    });
            alerte.setConstante(constante);
        }
        
        log.info("Sauvegarde alerte");
        return alerteRepository.save(alerte);
    }

    public Alerte modifierAlerte(Long id, AlerteRequest request) {
        log.info("Modifie alerte ID={}: niveau={}, constanteId={}",
                id, request.getNiveau(), request.getConstanteId());
        
        Alerte existante = alerteRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Alerte non trouvée avec ID: {}", id);
                    return new IllegalArgumentException("Alerte introuvable (id=" + id + ")");
                });

        existante.setNiveau(request.getNiveau());
        existante.setMessage(request.getMessage());
        existante.setDateDeclenchement(request.getDateDeclenchement());
        
        if (request.getConstanteId() != null) {
            log.info("Cherche constante avec ID: {}", request.getConstanteId());
            ConstanteVitale constante = constanteVitaleRepository.findById(request.getConstanteId())
                    .orElseThrow(() -> {
                        log.error("Constante non trouvée avec ID: {}", request.getConstanteId());
                        return new IllegalArgumentException(
                            "Constante introuvable (id=" + request.getConstanteId() + ")"
                        );
                    });
            existante.setConstante(constante);
        }

        log.info("Sauvegarde alerte modifiée");
        return alerteRepository.save(existante);
    }

    public void supprimerAlerte(Long id) {
        alerteRepository.deleteById(id);
    }

    public Alerte getAlerteById(Long id) {
        return alerteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alerte introuvable (id=" + id + ")"));
    }

    public List<Alerte> getAllAlertes() {
        return alerteRepository.findAll();
    }
    public List<Alerte> getAlertesByDossier(Long dossierId) {
        return alerteRepository.findByConstante_Dossier_IdDossierOrderByDateDeclenchementDesc(dossierId);
    }
}