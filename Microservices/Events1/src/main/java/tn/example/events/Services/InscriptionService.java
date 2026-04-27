package tn.example.events.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.example.events.Entities.*;
import tn.example.events.Repositories.EventRepository;
import tn.example.events.Repositories.InscriptionRepository;
import tn.example.events.Repositories.ParticipantRepository;
import tn.example.events.dto.CreateInscriptionFromUserDto;
import tn.example.events.dto.UserServiceSummaryDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class    InscriptionService {

    private final InscriptionRepository inscriptionRepository;
    private final EventRepository eventRepository;
    private final ParticipantRepository participantRepository;
    private final UserMicroserviceClient userMicroserviceClient;

    @Autowired
    private MailService mailService;

    @Autowired
    private SiegeService siegeService;

    public InscriptionService(InscriptionRepository inscriptionRepository,
                              EventRepository eventRepository,
                              ParticipantRepository participantRepository,
                              UserMicroserviceClient userMicroserviceClient) {
        this.inscriptionRepository = inscriptionRepository;
        this.eventRepository = eventRepository;
        this.participantRepository = participantRepository;
        this.userMicroserviceClient = userMicroserviceClient;
    }

    // ✅ CREATE depuis admin
    public Inscription addInscription(Long eventId, Long participantId,
                                      Statut statut, TypeParticipant typeParticipant) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        Inscription inscription = new Inscription();
        inscription.setEvent(event);
        inscription.setParticipant(participant);
        inscription.setDateInscription(LocalDateTime.now());
        inscription.setStatut(statut);
        inscription.setTypeParticipant(typeParticipant);

        return inscriptionRepository.save(inscription);
    }

    // ✅ CREATE depuis front avec gestion liste d'attente
    public Inscription create(Long eventId, Long participantId,
                              TypeParticipant typeParticipant) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Événement not found"));
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        Inscription inscription = new Inscription();
        inscription.setEvent(event);
        inscription.setParticipant(participant);
        inscription.setTypeParticipant(typeParticipant);
        inscription.setDateInscription(LocalDateTime.now());

        if (event.getCapacite() != null) {
            long inscritsConfirmes = inscriptionRepository
                    .countByEventAndStatut(event, Statut.CONFIRME);

            if (inscritsConfirmes >= event.getCapacite()) {
                // ✅ Complet → liste d'attente
                inscription.setStatut(Statut.LISTE_ATTENTE);
                Inscription saved = inscriptionRepository.save(inscription);
                mailService.sendListeAttenteNotification(saved);
                return saved;
            }
        }

        inscription.setStatut(Statut.EN_ATTENTE);
        return inscriptionRepository.save(inscription);
    }

    // ✅ CREATE front à partir de l'utilisateur connecté (microservice User)
    public Inscription createFromUserAccount(CreateInscriptionFromUserDto body) {
        if (body == null || body.getEventId() == null || body.getUserId() == null || body.getTypeParticipant() == null) {
            throw new RuntimeException("eventId, userId et typeParticipant sont obligatoires");
        }

        UserServiceSummaryDto user = userMicroserviceClient.getUserById(body.getUserId())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable dans le microservice User"));

        if (Boolean.FALSE.equals(user.getActive())) {
            throw new RuntimeException("Le compte utilisateur est inactif");
        }

        String email = firstNonBlank(body.getEmail(), user.getEmail());
        if (email == null) {
            throw new RuntimeException("Email utilisateur introuvable");
        }

        if (inscriptionRepository.existsNonCancelledForEventAndParticipantEmail(body.getEventId(), email, Statut.ANNULE)) {
            throw new RuntimeException("Vous êtes déjà inscrit à cet événement");
        }

        Participant participant = participantRepository.findByEmail(email)
                .orElseGet(() -> createParticipantFromUser(body, user, email));

        return create(body.getEventId(), participant.getIdParticipant(), body.getTypeParticipant());
    }

    private Participant createParticipantFromUser(CreateInscriptionFromUserDto body, UserServiceSummaryDto user, String email) {
        Participant participant = new Participant();
        participant.setEmail(email);
        participant.setTelephone(body.getTelephone());

        String displayName = firstNonBlank(body.getFullName(), user.getUsername(), email);
        String[] splitName = splitFullName(displayName);
        participant.setPrenom(splitName[0]);
        participant.setNom(splitName[1]);
        return participantRepository.save(participant);
    }

    private String[] splitFullName(String fullName) {
        String normalized = fullName == null ? "" : fullName.trim();
        if (normalized.isEmpty()) {
            return new String[]{"Utilisateur", "Connecté"};
        }
        String[] parts = normalized.split("\\s+", 2);
        if (parts.length == 1) {
            return new String[]{parts[0], "User"};
        }
        return new String[]{parts[0], parts[1]};
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }

    // ✅ UPDATE STATUT
    public Inscription updateStatut(Long id, Statut statut) {
        Inscription inscription = inscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inscription not found"));

        Statut ancienStatut = inscription.getStatut();
        inscription.setStatut(statut);
        Inscription saved = inscriptionRepository.save(inscription);

        // ✅ QR code si CONFIRME
        if (statut == Statut.CONFIRME) {
            mailService.sendQRCodeInscription(saved);
        }

        // ✅ Si ANNULE et ancien était CONFIRME → promouvoir liste d'attente
        if (statut == Statut.ANNULE && ancienStatut == Statut.CONFIRME) {
            promouvoirListeAttente(inscription.getEvent());
        }

        // ✅ Met à jour le siège selon le nouveau statut
        if (statut == Statut.CONFIRME) {
            siegeService.confirmerSiege(id); // → rouge
        } else if (statut == Statut.ANNULE || statut == Statut.ANNULE) {
            siegeService.libererSiege(id);   // → vert
        }


        return saved;
    }

    //  Promeut le premier en liste d'attente
    private void promouvoirListeAttente(Event event) {
        inscriptionRepository
                .findFirstByEventAndStatutOrderByDateInscriptionAsc(
                        event, Statut.LISTE_ATTENTE
                )
                .ifPresent(prochain -> {
                    prochain.setStatut(Statut.CONFIRME);
                    Inscription saved = inscriptionRepository.save(prochain);
                    mailService.sendQRCodeInscription(saved);
                    mailService.sendPromotionListeAttenteNotification(saved);
                });
    }

    // UPDATE
    public Inscription updateInscription(Inscription inscription) {
        return inscriptionRepository.save(inscription);
    }

    // DELETE
    public void deleteInscription(Long id) {
        inscriptionRepository.deleteById(id);
    }

    // GET BY ID
    public Optional<Inscription> getById(Long id) {
        return inscriptionRepository.findById(id);
    }

    // GET ALL
    public List<Inscription> getAll() {
        return inscriptionRepository.findAll();
    }

    public List<Inscription> filterInscriptions(
            Long idEvent,
            String statut,
            String type,
            String search,
            String dateDebut,
            String dateFin) {

        Statut statutEnum = (statut != null && !statut.isEmpty())
                ? Statut.valueOf(statut) : null;

        TypeParticipant typeEnum = (type != null && !type.isEmpty())
                ? TypeParticipant.valueOf(type) : null;

        LocalDateTime debut = (dateDebut != null && !dateDebut.isEmpty())
                ? LocalDateTime.parse(dateDebut + "T00:00:00") : null;

        LocalDateTime fin = (dateFin != null && !dateFin.isEmpty())
                ? LocalDateTime.parse(dateFin + "T23:59:59") : null;

        String searchParam = (search != null && !search.isEmpty()) ? search : null;

        return inscriptionRepository.filterInscriptions(
                idEvent, statutEnum, typeEnum, searchParam, debut, fin
        );
    }
}