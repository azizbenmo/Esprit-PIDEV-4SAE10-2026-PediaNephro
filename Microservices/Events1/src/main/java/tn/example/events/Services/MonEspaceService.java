package tn.example.events.Services;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.example.events.Entities.Inscription;
import tn.example.events.Entities.Participant;
import tn.example.events.Entities.Statut;
import tn.example.events.Repositories.InscriptionRepository;
import tn.example.events.Repositories.ParticipantRepository;
import tn.example.events.dto.InscriptionEventDTO;
import tn.example.events.dto.MonEspaceDTO;
import tn.example.events.dto.MonEspaceStatsDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonEspaceService {

    private final ParticipantRepository participantRepository;
    private final InscriptionRepository inscriptionRepository;

    public MonEspaceDTO getMonEspace(String email) {
        // Trouver le participant par email
        Participant participant = participantRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Participant non trouvé"));

        // Récupérer toutes ses inscriptions
        List<Inscription> inscriptions = inscriptionRepository
                .findByParticipant(participant);

        LocalDateTime now = LocalDateTime.now();

        // Séparer en catégories
        List<InscriptionEventDTO> aVenir = inscriptions.stream()
                .filter(i -> i.getStatut() == Statut.CONFIRME)
                .filter(i -> i.getEvent() != null &&
                        i.getEvent().getDateFin() != null &&
                        i.getEvent().getDateFin().isAfter(now))
                .map(this::toDTO)
                .collect(Collectors.toList());

        List<InscriptionEventDTO> historique = inscriptions.stream()
                .filter(i -> i.getEvent() != null &&
                        i.getEvent().getDateFin() != null &&
                        i.getEvent().getDateFin().isBefore(now))
                .map(this::toDTO)
                .collect(Collectors.toList());

        List<InscriptionEventDTO> enAttente = inscriptions.stream()
                .filter(i -> i.getStatut() == Statut.EN_ATTENTE ||
                        i.getStatut() == Statut.LISTE_ATTENTE)
                .filter(i -> i.getEvent() != null &&
                        i.getEvent().getDateFin() != null &&
                        i.getEvent().getDateFin().isAfter(now))
                .map(this::toDTO)
                .collect(Collectors.toList());

        // Stats
        MonEspaceStatsDTO stats = MonEspaceStatsDTO.builder()
                .totalInscriptions(inscriptions.size())
                .confirmes(inscriptions.stream()
                        .filter(i -> i.getStatut() == Statut.CONFIRME).count())
                .enAttente(inscriptions.stream()
                        .filter(i -> i.getStatut() == Statut.EN_ATTENTE).count())
                .annules(inscriptions.stream()
                        .filter(i -> i.getStatut() == Statut.ANNULE).count())
                .listeAttente(inscriptions.stream()
                        .filter(i -> i.getStatut() == Statut.LISTE_ATTENTE).count())
                .passees((long) historique.size())
                .aVenir((long) aVenir.size())
                .build();

        return MonEspaceDTO.builder()
                .idParticipant(participant.getIdParticipant())
                .nom(participant.getNom())
                .prenom(participant.getPrenom())
                .email(participant.getEmail())
                .telephone(participant.getTelephone())
                .stats(stats)
                .aVenir(aVenir)
                .historique(historique)
                .enAttente(enAttente)
                .build();
    }

    private InscriptionEventDTO toDTO(Inscription i) {
        return InscriptionEventDTO.builder()
                .idInscription(i.getIdInscription())
                .statut(i.getStatut().name())
                .typeParticipant(i.getTypeParticipant() != null ?
                        i.getTypeParticipant().name() : "")
                .dateInscription(i.getDateInscription())
                .idEvent(i.getEvent().getIdEvent())
                .nomEvent(i.getEvent().getNomEvent())
                .description(i.getEvent().getDescription())
                .lieu(i.getEvent().getLieu())
                .dateDebut(i.getEvent().getDateDebut())
                .dateFin(i.getEvent().getDateFin())
                .capacite(i.getEvent().getCapacite())
                .imageBase64(i.getEvent().getImageBase64())
                .build();
    }
}
