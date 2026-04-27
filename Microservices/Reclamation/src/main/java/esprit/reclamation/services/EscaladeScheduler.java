package esprit.reclamation.services;

import esprit.reclamation.entities.Reclamation;
import esprit.reclamation.entities.StatutReclamation;
import esprit.reclamation.repositories.ReclamationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EscaladeScheduler {

    private final ReclamationRepository reclamationRepository;
    private final ReclamationHistoryService reclamationHistoryService;

    @Scheduled(fixedDelay = 3_600_000)
    @Transactional
    public void escaladerSlaDepasses() {
        LocalDateTime maintenant = LocalDateTime.now();
        List<StatutReclamation> statutsOuverts = List.of(StatutReclamation.EN_ATTENTE, StatutReclamation.EN_COURS);
        List<Reclamation> candidates =
                reclamationRepository.findByEscaladeeIsFalseAndSlaDeadlineBeforeAndStatutIn(maintenant, statutsOuverts);

        for (Reclamation r : candidates) {
            StatutReclamation ancien = r.getStatut();
            r.setStatut(StatutReclamation.ESCALADEE);
            r.setEscaladee(true);
            reclamationRepository.save(r);
            reclamationHistoryService.enregistrer(
                    r.getId(),
                    ancien,
                    StatutReclamation.ESCALADEE,
                    null,
                    "Escalade automatique : échéance SLA dépassée.");
            log.warn("Escalade SLA — réclamation id={} (ancien statut={})", r.getId(), ancien);
        }
    }
}
