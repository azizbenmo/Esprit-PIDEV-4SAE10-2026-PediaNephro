package esprit.fraud.services;

import esprit.fraud.entities.FraudEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    /**
     * Envoie un email d'alerte de fraude de manière asynchrone.
     * Déclenché lorsque le score de fraude dépasse 50%.
     */
    @Async
    public void sendFraudAlertEmail(String toEmail, FraudEvent fraudEvent) {
        if (toEmail == null || toEmail.isBlank()) {
            log.warn("Impossible d'envoyer l'alerte de fraude: email non fourni pour le User ID {}", fraudEvent.getUserId());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("⚠️ PediaNephro - Alerte de Sécurité: Activité Suspecte Détectée");
            helper.setFrom("noreply@pedianephro.com");

            // Préparer le contexte Thymeleaf
            Context context = new Context();
            context.setVariable("userId", fraudEvent.getUserId());
            context.setVariable("action", fraudEvent.getAction());
            context.setVariable("score", String.format("%.1f", fraudEvent.getScore()));
            context.setVariable("ipAddress", fraudEvent.getIpAddress() != null ? fraudEvent.getIpAddress() : "Non disponible");
            context.setVariable("deviceInfo", fraudEvent.getDeviceInfo() != null ? fraudEvent.getDeviceInfo() : "Non disponible");
            context.setVariable("details", fraudEvent.getDetails());
            context.setVariable("date", fraudEvent.getCreatedAt().toString());
            context.setVariable("alertLevel", getAlertLevel(fraudEvent.getScore()));

            String htmlContent = templateEngine.process("fraud-alert", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email d'alerte de fraude envoyé avec succès à {} pour le User ID {}", toEmail, fraudEvent.getUserId());

        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email d'alerte de fraude à {}: {}", toEmail, e.getMessage(), e);
        }
    }

    /**
     * Détermine le niveau d'alerte en fonction du score.
     */
    private String getAlertLevel(Double score) {
        if (score >= 80.0) {
            return "CRITIQUE";
        } else if (score >= 65.0) {
            return "ÉLEVÉ";
        } else {
            return "MODÉRÉ";
        }
    }
}
