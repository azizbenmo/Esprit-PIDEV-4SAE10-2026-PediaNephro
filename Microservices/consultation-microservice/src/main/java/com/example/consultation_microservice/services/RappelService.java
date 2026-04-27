package com.example.consultation_microservice.services;


import com.example.consultation_microservice.entities.Consultation;
import com.example.consultation_microservice.entities.ConsultationStatus;
import com.example.consultation_microservice.repositories.ConsultationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RappelService {

    @Autowired
    private ConsultationRepository consultationRepository;

    @Autowired
    private EmailService emailService;

    // S'exécute toutes les heures
    @Scheduled(cron = "0 0 * * * *")
    public void envoyerRappels() {
        LocalDateTime maintenant = LocalDateTime.now();
        LocalDateTime dans24h = maintenant.plusHours(24);

        // Chercher les consultations acceptées dont la date souhaitée est dans ~24h
        List<Consultation> consultations = consultationRepository.findAll().stream()
                .filter(c -> c.getStatut() == ConsultationStatus.ACCEPTEE)
                .filter(c -> c.getDateSouhaitee() != null)
                .filter(c -> {
                    LocalDateTime date = c.getDateSouhaitee();
                    return date.isAfter(dans24h.minusMinutes(30))
                            && date.isBefore(dans24h.plusMinutes(30));
                })
                .toList();

        for (Consultation c : consultations) {
            String email = c.getPatient().getEmail();
            if (email != null && !email.isBlank()) {
                emailService.sendRappel(email, c);
                System.out.println("📧 Rappel envoyé à : " + email);
            }
        }
    }
}