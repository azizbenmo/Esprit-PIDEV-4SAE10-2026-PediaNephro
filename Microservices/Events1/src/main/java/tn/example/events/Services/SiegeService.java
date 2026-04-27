package tn.example.events.Services;


import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import tn.example.events.Entities.*;
import tn.example.events.Repositories.*;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SiegeService {

    private final SiegeRepository siegeRepository;
    private final EventRepository eventRepository;
    private final InscriptionRepository inscriptionRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public void genererSieges(Event event) {
        if (!siegeRepository.findByEventOrderByRangeeAscPositionAsc(event).isEmpty()) return;

        int capacite = event.getCapacite() != null ? event.getCapacite() : 30;
        int siegesParRangee = 10;
        List<Siege> sieges = new ArrayList<>();

        int totalRangees = (int) Math.ceil((double) capacite / siegesParRangee);

        for (int r = 0; r < totalRangees; r++) {
            String rangee = String.valueOf((char) ('A' + r));
            for (int p = 1; p <= siegesParRangee; p++) {
                if (sieges.size() >= capacite) break;
                Siege siege = new Siege();
                siege.setRangee(rangee);
                siege.setPosition(p);
                siege.setNumero(rangee + p);
                siege.setStatut(StatutSiege.LIBRE);
                siege.setEvent(event);
                sieges.add(siege);
            }
        }
        siegeRepository.saveAll(sieges);
    }

    public List<Siege> getSiegesParEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        genererSieges(event);
        return siegeRepository.findByEventOrderByRangeeAscPositionAsc(event);
    }

    /*public Siege reserverSiege(Long siegeId, Long inscriptionId) {
        Siege siege = siegeRepository.findById(siegeId)
                .orElseThrow(() -> new RuntimeException("Siege not found"));

        if (siege.getStatut() != StatutSiege.LIBRE) {
            throw new RuntimeException("Siège non disponible");
        }

        Inscription inscription = inscriptionRepository.findById(inscriptionId)
                .orElseThrow(() -> new RuntimeException("Inscription not found"));

        StatutSiege nouveauStatut = inscription.getStatut() == Statut.LISTE_ATTENTE
                ? StatutSiege.LISTE_ATTENTE
                : StatutSiege.PRIS;

        siege.setStatut(nouveauStatut);
        siege.setInscription(inscription);
        Siege saved = siegeRepository.save(siege);

        messagingTemplate.convertAndSend(
                "/topic/sieges/" + siege.getEvent().getIdEvent(),
                new SiegeUpdateDTO(saved.getIdSiege(), saved.getNumero(), saved.getStatut().name())
        );

        return saved;
    }*/

    public Siege reserverSiege(Long siegeId, Long inscriptionId) {
        Siege siege = siegeRepository.findById(siegeId)
                .orElseThrow(() -> new RuntimeException("Siege not found"));

        if (siege.getStatut() != StatutSiege.LIBRE) {
            throw new RuntimeException("Siège non disponible");
        }

        Inscription inscription = inscriptionRepository.findById(inscriptionId)
                .orElseThrow(() -> new RuntimeException("Inscription not found"));

        // ✅ Toujours LISTE_ATTENTE après inscription — admin confirme ensuite
        siege.setStatut(StatutSiege.LISTE_ATTENTE);
        siege.setInscription(inscription);
        Siege saved = siegeRepository.save(siege);

        messagingTemplate.convertAndSend(
                "/topic/sieges/" + siege.getEvent().getIdEvent(),
                new SiegeUpdateDTO(saved.getIdSiege(), saved.getNumero(), saved.getStatut().name())
        );

        return saved;
    }

    // ✅ Nouvelle méthode — confirmer un siège
    public Siege confirmerSiege(Long inscriptionId) {
        return siegeRepository.findByInscription_IdInscription(inscriptionId)
                .map(siege -> {
                    siege.setStatut(StatutSiege.PRIS);
                    Siege saved = siegeRepository.save(siege);
                    messagingTemplate.convertAndSend(
                            "/topic/sieges/" + siege.getEvent().getIdEvent(),
                            new SiegeUpdateDTO(saved.getIdSiege(), saved.getNumero(), saved.getStatut().name())
                    );
                    return saved;
                }).orElse(null);
    }

    // ✅ Nouvelle méthode — libérer un siège
    public Siege libererSiege(Long inscriptionId) {
        return siegeRepository.findByInscription_IdInscription(inscriptionId)
                .map(siege -> {
                    siege.setStatut(StatutSiege.LIBRE);
                    siege.setInscription(null);
                    Siege saved = siegeRepository.save(siege);
                    messagingTemplate.convertAndSend(
                            "/topic/sieges/" + siege.getEvent().getIdEvent(),
                            new SiegeUpdateDTO(saved.getIdSiege(), saved.getNumero(), saved.getStatut().name())
                    );
                    return saved;
                }).orElse(null);
    }

    public static class SiegeUpdateDTO {
        public Long idSiege;
        public String numero;
        public String statut;

        public SiegeUpdateDTO(Long idSiege, String numero, String statut) {
            this.idSiege = idSiege;
            this.numero = numero;
            this.statut = statut;
        }
    }
}
