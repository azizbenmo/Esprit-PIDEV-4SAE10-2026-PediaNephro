package com.example.dossiemedicale.services;

import com.example.dossiemedicale.DTO.HopitalProcheAutoResponse;
import com.example.dossiemedicale.DTO.LocalisationRequest;
import com.example.dossiemedicale.entities.Enfant;
import com.example.dossiemedicale.entities.Hopital;
import com.example.dossiemedicale.entities.LocalisationClient;
import com.example.dossiemedicale.repositoories.EnfantRepository;
import com.example.dossiemedicale.repositoories.HopitalRepository;
import com.example.dossiemedicale.repositoories.LocalisationClientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GeolocalisationService {

    private final EnfantRepository enfantRepository;
    private final LocalisationClientRepository localisationClientRepository;
    private final HopitalRepository hopitalRepository;

    private static final double DISTANCE_MIN_KM_POUR_SAUVEGARDE = 0.02; // 20 mètres
    private static final long TEMPS_MIN_MS_POUR_SAUVEGARDE = 10_000;   // 10 secondes

    public HopitalProcheAutoResponse enregistrerPositionEtTrouverHopitalProche(LocalisationRequest request) {

        Enfant enfant = enfantRepository.findById(request.getEnfantId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Enfant introuvable (id=" + request.getEnfantId() + ")"
                ));

        LocalisationClient dernierePosition = localisationClientRepository
                .findTopByEnfant_IdEnfantOrderByHorodatageDesc(request.getEnfantId())
                .orElse(null);

        Date maintenant = new Date();

        boolean doitSauvegarder = doitSauvegarderNouvellePosition(dernierePosition, request, maintenant);

        if (doitSauvegarder) {
            LocalisationClient localisation = new LocalisationClient();
            localisation.setEnfant(enfant);
            localisation.setLatitude(request.getLatitude());
            localisation.setLongitude(request.getLongitude());
            localisation.setPrecisionM(request.getPrecisionM());
            localisation.setSourceLocalisation(request.getSourceLocalisation());
            localisation.setHorodatage(maintenant);

            localisationClientRepository.save(localisation);
            dernierePosition = localisation;
        }

        List<Hopital> hopitaux = hopitalRepository.findAll();

        if (hopitaux.isEmpty()) {
            throw new IllegalArgumentException("Aucun hôpital disponible dans la base");
        }

        Hopital hopitalLePlusProche = null;
        double distanceMin = Double.MAX_VALUE;

        for (Hopital hopital : hopitaux) {
            double distance = calculerDistanceKm(
                    request.getLatitude(),
                    request.getLongitude(),
                    hopital.getLatitude(),
                    hopital.getLongitude()
            );

            if (distance < distanceMin) {
                distanceMin = distance;
                hopitalLePlusProche = hopital;
            }
        }

        String dateFormatee = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(dernierePosition.getHorodatage());

        return new HopitalProcheAutoResponse(
                hopitalLePlusProche.getIdHopital(),
                hopitalLePlusProche.getNom(),
                hopitalLePlusProche.getAdresse(),
                hopitalLePlusProche.getVille(),
                hopitalLePlusProche.getTelephone(),
                hopitalLePlusProche.getUrgence(),
                hopitalLePlusProche.getLatitude(),
                hopitalLePlusProche.getLongitude(),
                arrondir(distanceMin),
                request.getLatitude(),
                request.getLongitude(),
                new HopitalProcheAutoResponse.DateDerniereMaj(dateFormatee)
        );
    }

    public LocalisationClient getDernierePosition(Long enfantId) {
        return localisationClientRepository.findTopByEnfant_IdEnfantOrderByHorodatageDesc(enfantId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Aucune localisation trouvée pour l'enfant id=" + enfantId
                ));
    }

    public HopitalProcheAutoResponse getHopitalLePlusProcheDepuisDernierePosition(Long enfantId) {

        LocalisationClient dernierePosition = localisationClientRepository
                .findTopByEnfant_IdEnfantOrderByHorodatageDesc(enfantId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Aucune localisation trouvée pour l'enfant id=" + enfantId
                ));

        List<Hopital> hopitaux = hopitalRepository.findAll();

        if (hopitaux.isEmpty()) {
            throw new IllegalArgumentException("Aucun hôpital disponible dans la base");
        }

        Hopital hopitalLePlusProche = null;
        double distanceMin = Double.MAX_VALUE;

        for (Hopital hopital : hopitaux) {
            double distance = calculerDistanceKm(
                    dernierePosition.getLatitude(),
                    dernierePosition.getLongitude(),
                    hopital.getLatitude(),
                    hopital.getLongitude()
            );

            if (distance < distanceMin) {
                distanceMin = distance;
                hopitalLePlusProche = hopital;
            }
        }

        String dateFormatee = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(dernierePosition.getHorodatage());

        return new HopitalProcheAutoResponse(
                hopitalLePlusProche.getIdHopital(),
                hopitalLePlusProche.getNom(),
                hopitalLePlusProche.getAdresse(),
                hopitalLePlusProche.getVille(),
                hopitalLePlusProche.getTelephone(),
                hopitalLePlusProche.getUrgence(),
                hopitalLePlusProche.getLatitude(),
                hopitalLePlusProche.getLongitude(),
                arrondir(distanceMin),
                dernierePosition.getLatitude(),
                dernierePosition.getLongitude(),
                new HopitalProcheAutoResponse.DateDerniereMaj(dateFormatee)
        );
    }

    private boolean doitSauvegarderNouvellePosition(LocalisationClient anciennePosition,
                                                    LocalisationRequest nouvellePosition,
                                                    Date maintenant) {

        if (anciennePosition == null) {
            return true;
        }

        double distanceKm = calculerDistanceKm(
                anciennePosition.getLatitude(),
                anciennePosition.getLongitude(),
                nouvellePosition.getLatitude(),
                nouvellePosition.getLongitude()
        );

        long differenceTempsMs = maintenant.getTime() - anciennePosition.getHorodatage().getTime();

        return distanceKm >= DISTANCE_MIN_KM_POUR_SAUVEGARDE
                || differenceTempsMs >= TEMPS_MIN_MS_POUR_SAUVEGARDE;
    }

    private double calculerDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    private double arrondir(double valeur) {
        return Math.round(valeur * 100.0) / 100.0;
    }
}