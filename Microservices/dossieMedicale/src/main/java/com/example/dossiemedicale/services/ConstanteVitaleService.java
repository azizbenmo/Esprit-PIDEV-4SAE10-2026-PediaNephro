package com.example.dossiemedicale.services;

import com.example.dossiemedicale.DTOPrediction.PredictionRequest;
import com.example.dossiemedicale.DTOPrediction.PredictionResponse;
import com.example.dossiemedicale.entities.Alerte;
import com.example.dossiemedicale.entities.ConstantePrediction;
import com.example.dossiemedicale.entities.ConstanteVitale;
import com.example.dossiemedicale.repositoories.AlerteRepository;
import com.example.dossiemedicale.repositoories.ConstantePredictionRepository;
import com.example.dossiemedicale.repositoories.ConstanteVitaleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ConstanteVitaleService {

    @Autowired
    private RestTemplate restTemplate;

    private static final String FASTAPI_URL = "http://localhost:8000/predict_constante";

    @Autowired
    private ConstantePredictionRepository constantePredictionRepository;

    @Autowired
    private ConstanteVitaleRepository constanteVitaleRepository;

    @Autowired
    private AlerteRepository alerteRepository;

    @Autowired
    private MailService mailService;

    public ConstanteVitale ajouterConstanteVitale(ConstanteVitale constanteVitale) {

        // 1) Sauvegarde constante réelle
        constanteVitale.setIdConstante(null);
        ConstanteVitale saved = constanteVitaleRepository.save(constanteVitale);

        Long dossierId = saved.getDossier().getIdDossier();
        String type = saved.getType();

        // ==========================================================
        //  saved vs previous(par ID) -> alerte -> escalation mail
        // ==========================================================
        try {
            List<ConstanteVitale> prevList =
                    constanteVitaleRepository.findPreviousById(dossierId, type, saved.getIdConstante());

            ConstanteVitale previous = prevList.isEmpty() ? null : prevList.get(0);

            log.info("DEBUG dossierId={} type={} savedId={} savedVal={} previousId={} previousVal={}",
                    dossierId,
                    type,
                    saved.getIdConstante(),
                    saved.getValeur(),
                    previous != null ? previous.getIdConstante() : null,
                    previous != null ? previous.getValeur() : null
            );

            boolean isIncrease = previous != null
                    && saved.getValeur() != null
                    && previous.getValeur() != null
                    && saved.getValeur() > previous.getValeur();

            log.info("DEBUG isIncrease={}", isIncrease);

            if (isIncrease) {
                // 2.1 Créer alerte liée à la nouvelle constante
                Alerte alerte = new Alerte();
                alerte.setNiveau("WARNING");
                alerte.setMessage("Augmentation détectée pour " + type + " : "
                        + previous.getValeur() + " -> " + saved.getValeur());
                alerte.setDateDeclenchement(new Date());
                alerte.setConstante(saved);

                alerteRepository.save(alerte);
                log.info("✅ Alerte enregistrée (augmentation) pour constante id={}", saved.getIdConstante());

                // 2.2 Escalade mail si une alerte existait déjà sur la précédente
                boolean existedBefore = alerteRepository.existsByConstante_IdConstante(previous.getIdConstante());

                log.info("DEBUG existedBefore={} previousConstanteId={}",
                        existedBefore, previous.getIdConstante());

                if (existedBefore) {
                    String emailPatient = constanteVitaleRepository.findPatientEmailByDossierId(dossierId);

                    if (emailPatient != null && !emailPatient.isBlank()) {
                        String subject = buildPersonalizedSubject(type);
                        String body = buildPersonalizedEmailBody(
                                dossierId,
                                type,
                                previous.getValeur(),
                                saved.getValeur()
                        );

                        boolean ok = mailService.sendAlert(emailPatient, subject, body);
                        log.info("DEBUG mailSent={}", ok);

                        if (ok) {
                            log.info("📧 Email envoyé au patient {}", emailPatient);
                        } else {
                            log.warn("⚠️ Échec d'envoi de l'email au patient {}", emailPatient);
                        }
                    } else {
                        log.warn("⚠️ Escalade demandée mais patient sans email (dossierId={})", dossierId);
                    }
                }
            } else {
                log.info("DEBUG pas d'alerte: previous={} savedVal={}",
                        previous != null ? previous.getValeur() : null,
                        saved.getValeur());
            }

        } catch (Exception e) {
            log.error("❌ Erreur traitement alerte/email : {}", e.getMessage(), e);
        }

        // ==========================================================
        // 3) IA: prédiction si historique >= 5
        // ==========================================================
        try {
            List<ConstanteVitale> historiques =
                    constanteVitaleRepository.findByDossierIdDossierAndTypeOrderByDateMesureAsc(dossierId, type);

            List<Double> valeurs = historiques.stream()
                    .map(ConstanteVitale::getValeur)
                    .collect(Collectors.toList());

            if (valeurs.size() >= 5) {
                PredictionRequest request = new PredictionRequest(valeurs);
                PredictionResponse response =
                        restTemplate.postForObject(FASTAPI_URL, request, PredictionResponse.class);

                if (response != null && response.getNext_value() != null) {
                    ConstantePrediction prediction = new ConstantePrediction();
                    prediction.setType(type);
                    prediction.setValeurPredite(response.getNext_value());
                    prediction.setDatePrediction(new Date());
                    prediction.setDossier(saved.getDossier());
                    prediction.setConstanteSource(saved);

                    constantePredictionRepository.save(prediction);
                    log.info("✅ Prédiction enregistrée");
                } else {
                    log.warn("⚠️ Réponse IA vide ou next_value null pour dossierId={} type={}", dossierId, type);
                }
            } else {
                log.info("ℹ️ Historique insuffisant pour prédiction ({} valeurs) pour dossierId={} type={}",
                        valeurs.size(), dossierId, type);
            }

        } catch (Exception e) {
            log.error("❌ Erreur IA : {}", e.getMessage(), e);
        }

        return saved;
    }

    // =========================
    // Email personnalisé PediaNofro
    // =========================
    private String buildPersonalizedSubject(String type) {
        return "PediaNofro | Alerte médicale : augmentation répétée de " + getLibelleType(type);
    }

    private String buildPersonalizedEmailBody(Long dossierId, String type, Double previousValue, Double currentValue) {
        String dateFormatted = new SimpleDateFormat("dd/MM/yyyy à HH:mm").format(new Date());
        String libelleType = getLibelleType(type);

        return "<div style=\"margin:0; padding:0; background-color:#f4f7fb; font-family:Arial, Helvetica, sans-serif;\">"
                + "<div style=\"max-width:650px; margin:30px auto; background:#ffffff; border-radius:16px; overflow:hidden; "
                + "border:1px solid #e5eaf1; box-shadow:0 4px 14px rgba(0,0,0,0.08);\">"

                + "<div style=\"background:linear-gradient(135deg, #0d6efd, #0a58ca); padding:24px 30px; color:white;\">"
                + "<h1 style=\"margin:0; font-size:24px;\">PediaNofro</h1>"
                + "<p style=\"margin:8px 0 0; font-size:14px; opacity:0.95;\">"
                + "Plateforme de suivi médical pédiatrique"
                + "</p>"
                + "</div>"

                + "<div style=\"padding:30px;\">"
                + "<h2 style=\"margin-top:0; color:#dc3545; font-size:22px;\">Alerte médicale détectée</h2>"

                + "<p style=\"font-size:15px; color:#333; line-height:1.7;\">Bonjour,</p>"

                + "<p style=\"font-size:15px; color:#333; line-height:1.7;\">"
                + "Nous vous informons qu’une <strong>augmentation répétée</strong> a été détectée automatiquement "
                + "sur votre dossier médical via notre système de surveillance."
                + "</p>"

                + "<div style=\"background:#f8fafc; border:1px solid #dce6f2; border-left:5px solid #dc3545; "
                + "border-radius:10px; padding:18px; margin:24px 0;\">"
                + "<p style=\"margin:8px 0; font-size:14px; color:#222;\"><strong>Dossier médical :</strong> #"
                + dossierId + "</p>"
                + "<p style=\"margin:8px 0; font-size:14px; color:#222;\"><strong>Constante concernée :</strong> "
                + escapeHtml(libelleType) + "</p>"
                + "<p style=\"margin:8px 0; font-size:14px; color:#222;\"><strong>Valeur précédente :</strong> "
                + previousValue + "</p>"
                + "<p style=\"margin:8px 0; font-size:14px; color:#222;\"><strong>Valeur actuelle :</strong> "
                + currentValue + "</p>"
                + "<p style=\"margin:8px 0; font-size:14px; color:#222;\"><strong>Date de détection :</strong> "
                + dateFormatted + "</p>"
                + "</div>"

                + "<p style=\"font-size:15px; color:#333; line-height:1.7;\">"
                + "Nous vous recommandons de consulter votre médecin ou votre équipe médicale si nécessaire, "
                + "surtout en cas de symptômes inhabituels."
                + "</p>"

                + "<p style=\"font-size:15px; color:#333; line-height:1.7;\">"
                + "Ce message est envoyé automatiquement afin d’améliorer la réactivité de la prise en charge."
                + "</p>"

                + "<div style=\"margin-top:28px; padding:16px; background:#eef6ff; border-radius:10px; border:1px solid #d7e8ff;\">"
                + "<p style=\"margin:0; font-size:14px; color:#0b3d91;\">"
                + "<strong>Équipe PediaNofro</strong><br>"
                + "Merci de votre confiance."
                + "</p>"
                + "</div>"
                + "</div>"

                + "<div style=\"padding:18px 30px; background:#f8f9fb; border-top:1px solid #e9edf3;\">"
                + "<p style=\"margin:0; font-size:12px; color:#6c757d; line-height:1.6;\">"
                + "⚠️ Ceci est un message automatique. Merci de ne pas répondre directement à cet email."
                + "</p>"
                + "</div>"

                + "</div>"
                + "</div>";
    }

    private String getLibelleType(String type) {
        if (type == null) {
            return "Constante vitale";
        }

        switch (type.toUpperCase()) {
            case "POULS":
                return "Fréquence cardiaque (Pouls)";
            case "TENSION_ARTERIELLE":
                return "Tension artérielle";
            case "FREQUENCE_RESPIRATOIRE":
                return "Fréquence respiratoire";
            case "TEMPERATURE":
                return "Température corporelle";
            case "SATURATION_OXYGENE":
                return "Saturation en oxygène";
            default:
                return type;
        }
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    public ConstanteVitale modifierConstanteVitale(Long id, ConstanteVitale constanteVitale) {
        ConstanteVitale existante = constanteVitaleRepository.findById(id).orElse(null);
        if (existante == null) {
            return null;
        }

        constanteVitale.setIdConstante(id);
        return constanteVitaleRepository.save(constanteVitale);
    }

    public void supprimerConstanteVitale(Long id) {
        constanteVitaleRepository.deleteById(id);
    }

    public ConstanteVitale getConstanteVitaleById(Long id) {
        return constanteVitaleRepository.findById(id).orElse(null);
    }

    public List<ConstanteVitale> getAllConstantesVitales() {
        return constanteVitaleRepository.findAll();
    }

    public List<ConstantePrediction> getPredictionsByDossier(Long idDossier) {
        return constantePredictionRepository.findByDossierIdDossierOrderByDatePredictionAsc(idDossier);
    }

    public List<ConstantePrediction> getPredictionsByDossierAndType(Long idDossier, String type) {
        return constantePredictionRepository.findByDossierIdDossierAndTypeOrderByDatePredictionAsc(idDossier, type);
    }
}