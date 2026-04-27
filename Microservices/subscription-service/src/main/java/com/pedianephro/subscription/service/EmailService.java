package com.pedianephro.subscription.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Value("${spring.mail.username:noreply@pedianephro.com}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    private String getHtmlTemplate(String content) {
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head>" +
               "<style>" +
               "  body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f7fe; margin: 0; padding: 0; }" +
               "  .container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }" +
               "  .header { background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%); padding: 30px; text-align: center; color: white; }" +
               "  .header h1 { margin: 0; font-size: 24px; letter-spacing: 1px; }" +
               "  .content { padding: 40px; color: #2d3748; line-height: 1.6; }" +
               "  .footer { background-color: #f8faff; padding: 20px; text-align: center; color: #a0aec0; font-size: 12px; }" +
               "  .button { display: inline-block; padding: 12px 30px; background: #1e3c72; color: white !important; text-decoration: none; border-radius: 8px; font-weight: bold; margin-top: 20px; }" +
               "  .highlight { color: #1e3c72; font-weight: bold; }" +
               "  .info-box { background-color: #edf2f7; border-radius: 12px; padding: 20px; margin: 20px 0; border-left: 4px solid #1e3c72; }" +
               "</style>" +
               "</head>" +
               "<body>" +
               "  <div class='container'>" +
               "    <div class='header'>" +
               "      <h1>🏥 PEDIA-NEPHRO</h1>" +
               "    </div>" +
               "    <div class='content'>" +
               content +
               "    </div>" +
               "    <div class='footer'>" +
               "      &copy; 2026 Pedia-Nephro. Tous droits réservés.<br>" +
               "      Plateforme de Néphrologie Pédiatrique" +
               "    </div>" +
               "  </div>" +
               "</body>" +
               "</html>";
    }

    public void sendSubscriptionConfirmation(String to, String patientName, String planName, String amount) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Pedia-Nephro Support");
            helper.setTo(to);
            helper.setSubject("Confirmation de votre abonnement Pedia-Nephro");

            String htmlContent = getHtmlTemplate(
                "<h2>Bonjour <span class='highlight'>" + patientName + "</span>,</h2>" +
                "<p>Nous avons le plaisir de vous confirmer la validation de votre nouvel abonnement.</p>" +
                "<div class='info-box'>" +
                "  <strong>Offre choisie :</strong> " + planName + "<br>" +
                "  <strong>Montant réglé :</strong> <span class='highlight'>" + amount + "</span><br>" +
                "  <strong>Date d'activation :</strong> " + LocalDate.now().format(formatter) + "" +
                "</div>" +
                "<p>Vous pouvez désormais accéder à l'ensemble des fonctionnalités de votre espace patient.</p>" +
                "<center><a href='http://localhost:4200/login' class='button'>Accéder à mon espace</a></center>" +
                "<p>Merci de votre confiance,<br>L'équipe Pedia-Nephro</p>"
            );

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            // Log simple en cas d'erreur
            System.err.println("Erreur envoi mail : " + e.getMessage());
        }
    }

    public void sendExpirationReminder(String to, String patientName, String planName, LocalDate endDate) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Pedia-Nephro Support");
            helper.setTo(to);
            helper.setSubject("Rappel : Votre abonnement Pedia-Nephro expire bientôt");

            String htmlContent = getHtmlTemplate(
                "<h2>Bonjour <span class='highlight'>" + patientName + "</span>,</h2>" +
                "<p>Ceci est un rappel concernant votre abonnement actuel à notre plateforme.</p>" +
                "<div class='info-box' style='border-left-color: #e53e3e;'>" +
                "  <strong>Offre actuelle :</strong> " + planName + "<br>" +
                "  <strong>Date d'échéance :</strong> <span style='color: #e53e3e; font-weight: bold;'>" + endDate.format(formatter) + "</span>" +
                "</div>" +
                "<p>Pour continuer à profiter de vos services, nous vous invitons à renouveler votre abonnement.</p>" +
                "<center><a href='http://localhost:4200/patient/abonnement' class='button' style='background: #e53e3e;'>Renouveler mon offre</a></center>" +
                "<p>À bientôt,<br>L'équipe Pedia-Nephro</p>"
            );

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Erreur envoi mail rappel : " + e.getMessage());
        }
    }

    public void sendRenewalProposal(String to,
                                   String patientName,
                                   String currentPlan,
                                   String suggestedPlan,
                                   String adjustmentType,
                                   Double difference,
                                   String confirmationLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Pedia-Nephro Support");
            helper.setTo(to);
            helper.setSubject("Renouvellement : proposition d'ajustement de votre plan");

            String diffText = difference == null ? "" : (difference >= 0 ? "+" : "") + String.format("%.0f", difference) + " DT/mois";

            String htmlContent = getHtmlTemplate(
                    "<h2>Bonjour <span class='highlight'>" + patientName + "</span>,</h2>" +
                            "<p>Votre abonnement arrive bientôt à échéance. Nous vous proposons un ajustement basé sur votre profil et votre usage.</p>" +
                            "<div class='info-box'>" +
                            "  <strong>Plan actuel :</strong> " + currentPlan + "<br>" +
                            "  <strong>Suggestion :</strong> " + suggestedPlan + " (" + adjustmentType + ")<br>" +
                            (diffText.isBlank() ? "" : "  <strong>Impact :</strong> <span class='highlight'>" + diffText + "</span><br>") +
                            "</div>" +
                            "<p>Vous pouvez confirmer ce renouvellement depuis votre espace patient.</p>" +
                            "<center><a href='" + confirmationLink + "' class='button'>Voir la proposition</a></center>" +
                            "<p>Merci de votre confiance,<br>L'équipe Pedia-Nephro</p>"
            );

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Erreur envoi mail proposition : " + e.getMessage());
        }
    }
}
