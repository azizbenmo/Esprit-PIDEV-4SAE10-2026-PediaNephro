package com.example.consultation_microservice.services;

import com.example.consultation_microservice.entities.Consultation;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    @Async
    public void sendConfirmation(String to, Consultation consultation) {
        sendHtmlEmail(
                to,
                "Confirmation de votre demande de consultation",
                buildConfirmationEmail(consultation),
                "confirmation",
                consultation
        );
    }

    @Async
    public void sendAnnulation(String to, Consultation consultation) {
        sendHtmlEmail(
                to,
                "Annulation de votre consultation",
                buildAnnulationEmail(consultation),
                "annulation",
                consultation
        );
    }

   /* private String buildConfirmationEmail(Consultation c) {
        return """
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px;">
                <h2 style="color: #2c7be5;">Demande de consultation reÃ§ue âœ…</h2>
                <p>Bonjour <strong>%s %s</strong>,</p>
                <p>Votre demande de consultation a bien Ã©tÃ© enregistrÃ©e.</p>
                <table style="border-collapse: collapse; width: 100%%;">
                    <tr><td style="padding: 8px; border: 1px solid #ddd;"><strong>SpÃ©cialitÃ©</strong></td>
                        <td style="padding: 8px; border: 1px solid #ddd;">%s</td></tr>
                    <tr><td style="padding: 8px; border: 1px solid #ddd;"><strong>Motif</strong></td>
                        <td style="padding: 8px; border: 1px solid #ddd;">%s</td></tr>
                    <tr><td style="padding: 8px; border: 1px solid #ddd;"><strong>MÃ©decin assignÃ©</strong></td>
                        <td style="padding: 8px; border: 1px solid #ddd;">Dr. %s %s</td></tr>
                    <tr><td style="padding: 8px; border: 1px solid #ddd;"><strong>Date souhaitÃ©e</strong></td>
                        <td style="padding: 8px; border: 1px solid #ddd;">%s</td></tr>
                    <tr><td style="padding: 8px; border: 1px solid #ddd;"><strong>Statut</strong></td>
                        <td style="padding: 8px; border: 1px solid #ddd;">%s</td></tr>
                </table>
                <p style="margin-top: 20px;">Vous serez contactÃ©(e) pour confirmer la date dÃ©finitive.</p>
                <p>Cordialement,<br/><strong>L'Ã©quipe mÃ©dicale</strong></p>
            </body>
            </html>
            """.formatted(
                c.getPatient().getPrenom(),
                c.getPatient().getNom(),
                c.getSpecialite(),
                c.getMotif(),
                c.getMedecin().getPrenom(),
                c.getMedecin().getNom(),
                c.getDateSouhaitee(),
                c.getStatut()
        );
    }*/

    private String buildConfirmationEmail(Consultation c) {
        return """
        <html>
        <body style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
            <div style="max-width: 600px; margin: 0 auto; border: 1px solid #eee; border-radius: 8px; overflow: hidden;">
                
                <!-- Header -->
                <div style="background-color: #2c7be5; padding: 30px; text-align: center;">
                    <h1 style="color: white; margin: 0; font-size: 24px;">Demande de Consultation Reçue</h1>
                </div>
                
                <!-- Body -->
                <div style="padding: 30px;">
                    <p style="font-size: 16px;">Bonjour <strong>%s %s</strong>,</p>
                    <p style="font-size: 15px; line-height: 1.6;">
                        Votre demande de consultation a bien été enregistrée. 
                        Vous trouverez ci-dessous le récapitulatif de votre demande :
                    </p>
                    
                    <!-- Récapitulatif -->
                    <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                        <tr style="background-color: #f0f6ff;">
                            <td style="padding: 12px 15px; border: 1px solid #dce8ff; font-weight: bold; width: 40%%;">Spécialité</td>
                            <td style="padding: 12px 15px; border: 1px solid #dce8ff;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 12px 15px; border: 1px solid #dce8ff; font-weight: bold;">Médecin assigné</td>
                            <td style="padding: 12px 15px; border: 1px solid #dce8ff;">Dr. %s %s</td>
                        </tr>
                        <tr style="background-color: #f0f6ff;">
                            <td style="padding: 12px 15px; border: 1px solid #dce8ff; font-weight: bold;">Date souhaitée</td>
                            <td style="padding: 12px 15px; border: 1px solid #dce8ff;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 12px 15px; border: 1px solid #dce8ff; font-weight: bold;">Motif</td>
                            <td style="padding: 12px 15px; border: 1px solid #dce8ff;">%s</td>
                        </tr>
                        <tr style="background-color: #f0f6ff;">
                            <td style="padding: 12px 15px; border: 1px solid #dce8ff; font-weight: bold;">Statut</td>
                            <td style="padding: 12px 15px; border: 1px solid #dce8ff;">
                                <span style="background-color: #d4edda; color: #155724; padding: 4px 10px; border-radius: 12px; font-size: 13px;">
                                    %s
                                </span>
                            </td>
                        </tr>
                    </table>
                    
                    <div style="background-color: #f0f6ff; border-left: 4px solid #2c7be5; padding: 15px; margin: 20px 0;">
                        <p style="margin: 0; color: #2c7be5; font-weight: bold;">Information importante</p>
                        <p style="margin: 5px 0 0 0; font-size: 14px;">
                            Vous serez prochainement contacté(e) pour vous informer de la suite donnée à votre demande de consultation.
                        </p>
                    </div>
                    
                    <p style="font-size: 15px; margin-top: 30px;">
                        Cordialement,<br/>
                        <strong>L'équipe médicale</strong>
                    </p>
                </div>
                
                <!-- Footer -->
                <div style="background-color: #f5f5f5; padding: 15px; text-align: center; font-size: 12px; color: #999;">
                    Cet email a été envoyé automatiquement, merci de ne pas y répondre.
                </div>
                
            </div>
        </body>
        </html>
        """.formatted(
                c.getPatient().getPrenom(),
                c.getPatient().getNom(),
                c.getSpecialite(),
                c.getMedecin().getPrenom(),
                c.getMedecin().getNom(),
                c.getDateSouhaitee(),
                c.getMotif(),
                c.getStatut()
        );
    }

    private String buildAnnulationEmail(Consultation c) {
        return """
        <html>
        <body style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
            <div style="max-width: 600px; margin: 0 auto; border: 1px solid #eee; border-radius: 8px; overflow: hidden;">
                
                <!-- Header -->
                <div style="background-color: #e53e3e; padding: 30px; text-align: center;">
                    <h1 style="color: white; margin: 0; font-size: 24px;">Consultation Annulée</h1>
                </div>
                
                <!-- Body -->
                <div style="padding: 30px;">
                    <p style="font-size: 16px;">Bonjour <strong>%s %s</strong>,</p>
                    <p style="font-size: 15px; line-height: 1.6;">
                        Nous vous informons que votre consultation prévue le 
                        <strong>%s</strong> avec <strong>Dr. %s %s</strong> 
                        (spécialité: <strong>%s</strong>) a bien été annulée.
                    </p>
                    
                    <div style="background-color: #fff8f8; border-left: 4px solid #e53e3e; padding: 15px; margin: 20px 0;">
                        <p style="margin: 0; color: #e53e3e; font-weight: bold;">Motif de la consultation annulée :</p>
                        <p style="margin: 5px 0 0 0;">%s</p>
                    </div>
                    
                    <p style="font-size: 15px; line-height: 1.6;">
                        Si vous souhaitez reprendre un rendez-vous, vous pouvez effectuer 
                        une nouvelle demande de consultation depuis votre espace patient.
                    </p>
                    
                    <p style="font-size: 15px; margin-top: 30px;">
                        Cordialement,<br/>
                        <strong>L'équipe médicale</strong>
                    </p>
                </div>
                
                <!-- Footer -->
                <div style="background-color: #f5f5f5; padding: 15px; text-align: center; font-size: 12px; color: #999;">
                    Cet email a été envoyé automatiquement, merci de ne pas y répondre.
                </div>
                
            </div>
        </body>
        </html>
        """.formatted(
                c.getPatient().getPrenom(),
                c.getPatient().getNom(),
                c.getDateSouhaitee(),
                c.getMedecin().getPrenom(),
                c.getMedecin().getNom(),
                c.getSpecialite(),
                c.getMotif()
        );
    }

    @Async
    public void sendAcceptation(String to, Consultation consultation) {
        sendHtmlEmail(
                to,
                "Votre consultation a été acceptée",
                buildAcceptationEmail(consultation),
                "acceptation",
                consultation
        );
    }

    @Async
    public void sendRefus(String to, Consultation consultation) {
        sendHtmlEmail(
                to,
                "Votre consultation a été refusée",
                buildRefusEmail(consultation),
                "refus",
                consultation
        );
    }

    private String buildAcceptationEmail(Consultation c) {
        return """
        <html>
        <body style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
            <div style="max-width: 600px; margin: 0 auto; border: 1px solid #eee; border-radius: 8px; overflow: hidden;">
                <div style="background-color: #28a745; padding: 30px; text-align: center;">
                    <h1 style="color: white; margin: 0; font-size: 24px;">Consultation Acceptée</h1>
                </div>
                <div style="padding: 30px;">
                    <p style="font-size: 16px;">Bonjour <strong>%s %s</strong>,</p>
                    <p style="font-size: 15px; line-height: 1.6;">
                        Nous avons le plaisir de vous informer que votre demande de consultation a été <strong>acceptée</strong>.
                    </p>
                    <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                        <tr style="background-color: #f0fff4;">
                            <td style="padding: 12px 15px; border: 1px solid #c3e6cb; font-weight: bold; width: 40%%;">Spécialité</td>
                            <td style="padding: 12px 15px; border: 1px solid #c3e6cb;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 12px 15px; border: 1px solid #c3e6cb; font-weight: bold;">Médecin</td>
                            <td style="padding: 12px 15px; border: 1px solid #c3e6cb;">Dr. %s %s</td>
                        </tr>
                        <tr style="background-color: #f0fff4;">
                            <td style="padding: 12px 15px; border: 1px solid #c3e6cb; font-weight: bold;">Date souhaitée</td>
                            <td style="padding: 12px 15px; border: 1px solid #c3e6cb;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 12px 15px; border: 1px solid #c3e6cb; font-weight: bold;">Motif</td>
                            <td style="padding: 12px 15px; border: 1px solid #c3e6cb;">%s</td>
                        </tr>
                    </table>
                    <div style="background-color: #f0fff4; border-left: 4px solid #28a745; padding: 15px; margin: 20px 0;">
                        <p style="margin: 0; color: #28a745; font-weight: bold;">Prochaine étape</p>
                        <p style="margin: 5px 0 0 0; font-size: 14px;">
                            Vous serez prochainement contacté(e) afin de vous rappeler votre consultation.
                        </p>
                    </div>
                    <p style="font-size: 15px; margin-top: 30px;">
                        Cordialement,<br/><strong>L'équipe médicale</strong>
                    </p>
                </div>
                <div style="background-color: #f5f5f5; padding: 15px; text-align: center; font-size: 12px; color: #999;">
                    Cet email a été envoyé automatiquement, merci de ne pas y répondre.
                </div>
            </div>
        </body>
        </html>
        """.formatted(
                c.getPatient().getPrenom(),
                c.getPatient().getNom(),
                c.getSpecialite(),
                c.getMedecin().getPrenom(),
                c.getMedecin().getNom(),
                c.getDateSouhaitee(),
                c.getMotif()
        );
    }

    private String buildRefusEmail(Consultation c) {
        return """
        <html>
        <body style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
            <div style="max-width: 600px; margin: 0 auto; border: 1px solid #eee; border-radius: 8px; overflow: hidden;">
                <div style="background-color: #dc3545; padding: 30px; text-align: center;">
                    <h1 style="color: white; margin: 0; font-size: 24px;">Consultation Refusée </h1>
                </div>
                <div style="padding: 30px;">
                    <p style="font-size: 16px;">Bonjour <strong>%s %s</strong>,</p>
                    <p style="font-size: 15px; line-height: 1.6;">
                        Nous vous informons que votre demande de consultation en <strong>%s</strong>
                        avec <strong>Dr. %s %s</strong> prévue le <strong>%s</strong> a été <strong>refusé</strong>.
                    </p>
                    <div style="background-color: #fff8f8; border-left: 4px solid #dc3545; padding: 15px; margin: 20px 0;">
                        <p style="margin: 0; color: #dc3545; font-weight: bold;">Que faire ?</p>
                        <p style="margin: 5px 0 0 0; font-size: 14px;">
                            Vous pouvez effectuer une nouvelle demande de consultation depuis votre espace patient,
                            en choisissant un autre médecin ou une autre date.
                        </p>
                    </div>
                    <p style="font-size: 15px; margin-top: 30px;">
                        Cordialement,<br/><strong>L'équipe médicale</strong>
                    </p>
                </div>
                <div style="background-color: #f5f5f5; padding: 15px; text-align: center; font-size: 12px; color: #999;">
                    Cet email a été envoyé automatiquement, merci de ne pas y répondre.
                </div>
            </div>
        </body>
        </html>
        """.formatted(
                c.getPatient().getPrenom(),
                c.getPatient().getNom(),
                c.getSpecialite(),
                c.getMedecin().getPrenom(),
                c.getMedecin().getNom(),
                c.getDateSouhaitee()
        );
    }

    @Async
    public void sendRappel(String to, Consultation consultation) {
        sendHtmlEmail(
                to,
                "Rappel : Votre consultation est demain",
                buildRappelEmail(consultation),
                "rappel",
                consultation
        );
    }

    private String buildRappelEmail(Consultation c) {
        return """
        <html>
        <body style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
            <div style="max-width: 600px; margin: 0 auto; border: 1px solid #eee; border-radius: 8px; overflow: hidden;">
                <div style="background-color: #f39c12; padding: 30px; text-align: center;">
                    <h1 style="color: white; margin: 0; font-size: 24px;">â° Rappel de Consultation</h1>
                </div>
                <div style="padding: 30px;">
                    <p style="font-size: 16px;">Bonjour <strong>%s %s</strong>,</p>
                    <p style="font-size: 15px; line-height: 1.6;">
                        Nous vous rappelons que vous avez une consultation <strong>demain</strong>.
                    </p>
                    <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                        <tr style="background-color: #fff8ee;">
                            <td style="padding: 12px 15px; border: 1px solid #fde8c0; font-weight: bold; width: 40%%;">Spécialité</td>
                            <td style="padding: 12px 15px; border: 1px solid #fde8c0;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 12px 15px; border: 1px solid #fde8c0; font-weight: bold;">Médecin</td>
                            <td style="padding: 12px 15px; border: 1px solid #fde8c0;">Dr. %s %s</td>
                        </tr>
                        <tr style="background-color: #fff8ee;">
                            <td style="padding: 12px 15px; border: 1px solid #fde8c0; font-weight: bold;">Date et heure</td>
                            <td style="padding: 12px 15px; border: 1px solid #fde8c0;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 12px 15px; border: 1px solid #fde8c0; font-weight: bold;">Motif</td>
                            <td style="padding: 12px 15px; border: 1px solid #fde8c0;">%s</td>
                        </tr>
                    </table>
                    <div style="background-color: #fff8ee; border-left: 4px solid #f39c12; padding: 15px; margin: 20px 0;">
                        <p style="margin: 0; color: #f39c12; font-weight: bold;">Ã€ ne pas oublier</p>
                        <p style="margin: 5px 0 0 0; font-size: 14px;">
                            Pensez Ã  apporter vos documents médicaux et Ã  vous présenter 10 minutes avant l'heure prévue.
                        </p>
                    </div>
                    <p style="font-size: 15px; margin-top: 30px;">
                        Cordialement,<br/><strong>L'équipe médicale</strong>
                    </p>
                </div>
                <div style="background-color: #f5f5f5; padding: 15px; text-align: center; font-size: 12px; color: #999;">
                    Cet email a été envoyé automatiquement, merci de ne pas y répondre.
                </div>
            </div>
        </body>
        </html>
        """.formatted(
                c.getPatient().getPrenom(),
                c.getPatient().getNom(),
                c.getSpecialite(),
                c.getMedecin().getPrenom(),
                c.getMedecin().getNom(),
                c.getDateSouhaitee(),
                c.getMotif()
        );
    }

    @Async
    public void sendAlerteUrgente(String to, Consultation consultation) {
        sendHtmlEmail(
                to,
                "ALERTE - Consultation urgente recue",
                buildAlerteUrgenteEmail(consultation),
                "alerte-urgente",
                consultation
        );
    }

    private String buildAlerteUrgenteEmail(Consultation c) {
        return """
    <html>
    <body style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
        <div style="max-width: 600px; margin: 0 auto; border: 2px solid #dc3545; border-radius: 8px; overflow: hidden;">

            <!-- Header rouge vif -->
            <div style="background-color: #dc3545; padding: 30px; text-align: center;">
                <h1 style="color: white; margin: 0; font-size: 26px;">ðŸš¨ CONSULTATION URGENTE</h1>
                <p style="color: #ffe0e0; margin: 8px 0 0 0; font-size: 14px;">
                    Action immédiate requise
                </p>
            </div>

            <!-- Body -->
            <div style="padding: 30px;">
                <p style="font-size: 16px;">Bonjour <strong>Dr. %s %s</strong>,</p>
                <p style="font-size: 15px; line-height: 1.6;">
                    Une demande de consultation classée <strong style="color: #dc3545;">URGENTE</strong>
                    vient d'Ãªtre soumise et nécessite votre attention immédiate.
                </p>

                <!-- Score urgence -->
                <div style="background-color: #fff0f0; border: 2px solid #dc3545; border-radius: 8px;
                            padding: 16px; margin: 20px 0; text-align: center;">
                    <p style="margin: 0; font-size: 13px; color: #dc3545; font-weight: bold; text-transform: uppercase;">
                        Score d'urgence
                    </p>
                    <p style="margin: 6px 0 0 0; font-size: 32px; font-weight: bold; color: #dc3545;">
                        %d / 100
                    </p>
                </div>

                <!-- Infos consultation -->
                <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                    <tr style="background-color: #fff0f0;">
                        <td style="padding: 12px 15px; border: 1px solid #f5c6cb; font-weight: bold; width: 40%%;">ðŸ‘¤ Patient</td>
                        <td style="padding: 12px 15px; border: 1px solid #f5c6cb;">%s %s</td>
                    </tr>
                    <tr>
                        <td style="padding: 12px 15px; border: 1px solid #f5c6cb; font-weight: bold;">ðŸ©º Spécialité</td>
                        <td style="padding: 12px 15px; border: 1px solid #f5c6cb;">%s</td>
                    </tr>
                    <tr style="background-color: #fff0f0;">
                        <td style="padding: 12px 15px; border: 1px solid #f5c6cb; font-weight: bold;">ðŸ“‹ Motif</td>
                        <td style="padding: 12px 15px; border: 1px solid #f5c6cb;">%s</td>
                    </tr>
                    <tr>
                        <td style="padding: 12px 15px; border: 1px solid #f5c6cb; font-weight: bold;">ðŸ“… Date souhaitée</td>
                        <td style="padding: 12px 15px; border: 1px solid #f5c6cb;">%s</td>
                    </tr>
                </table>

                <!-- Justification triage -->
                <div style="background-color: #fff0f0; border-left: 4px solid #dc3545; padding: 15px; margin: 20px 0;">
                    <p style="margin: 0; color: #dc3545; font-weight: bold;">ðŸ” Analyse du triage automatique</p>
                    <p style="margin: 8px 0 0 0; font-size: 14px; line-height: 1.6;">%s</p>
                </div>

                <div style="background-color: #fff8f8; border: 1px solid #f5c6cb; border-radius: 6px;
                            padding: 15px; margin: 20px 0; text-align: center;">
                    <p style="margin: 0; font-size: 15px; color: #dc3545; font-weight: bold;">
                        âš¡ Veuillez traiter cette demande en priorité depuis votre espace médecin.
                    </p>
                </div>

                <p style="font-size: 15px; margin-top: 30px;">
                    Cordialement,<br/>
                    <strong>Système de triage automatique â€” PédiaNephro</strong>
                </p>
            </div>

            <!-- Footer -->
            <div style="background-color: #f5f5f5; padding: 15px; text-align: center; font-size: 12px; color: #999;">
                Cet email a été envoyé automatiquement par le système de triage. Merci de ne pas y répondre.
            </div>

        </div>
    </body>
    </html>
    """.formatted(
                c.getMedecin().getPrenom(),
                c.getMedecin().getNom(),
                c.getScoreUrgence(),
                c.getPatient().getPrenom(),
                c.getPatient().getNom(),
                c.getSpecialite(),
                c.getMotif(),
                c.getDateSouhaitee(),
                c.getJustificationTriage()
        );
    }

    private void sendHtmlEmail(String to, String subject, String html, String type, Consultation consultation) {
        String dest = normalizeEmail(to);
        if (dest == null) {
            log.warn("[MAIL:{}] destinataire invalide pour consultation id={} : {}", type, safeConsultationId(consultation), to);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(dest);
            if (fromAddress != null && !fromAddress.isBlank()) {
                helper.setFrom(fromAddress.trim());
            }
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("[MAIL:{}] envoye a {} pour consultation id={}", type, dest, safeConsultationId(consultation));
        } catch (MessagingException e) {
            log.error("[MAIL:{}] echec envoi a {} pour consultation id={} : {}", type, dest, safeConsultationId(consultation), e.getMessage(), e);
        } catch (Exception e) {
            log.error("[MAIL:{}] erreur inattendue pour {} consultation id={} : {}", type, dest, safeConsultationId(consultation), e.getMessage(), e);
        }
    }

    private String normalizeEmail(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isBlank()) {
            return null;
        }
        try {
            InternetAddress address = new InternetAddress(trimmed);
            address.validate();
            return address.getAddress();
        } catch (AddressException ex) {
            return null;
        }
    }

    private Long safeConsultationId(Consultation consultation) {
        return consultation == null ? null : consultation.getId();
    }
}

