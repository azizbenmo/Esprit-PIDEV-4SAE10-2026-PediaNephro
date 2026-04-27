package tn.example.events.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tn.example.events.Entities.Inscription;
import tn.example.events.Entities.Partenariat;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Autowired
    private CertificatService certificatService;

    @Autowired
    private QRCodeService qrCodeService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.mail.admin}")
    private String adminEmail;

    // Mail à l'admin quand nouvelle demande
    @Async  //  envoi en arrière-plan
    public void sendDemandePartenariatToAdmin(Partenariat p) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(adminEmail);
            helper.setSubject("🤝 Nouvelle demande de partenariat — " + p.getNomEntreprise());
            helper.setText(buildAdminHtml(p), true);

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Erreur envoi mail admin: " + e.getMessage());
        }
    }

    // Mail de confirmation à l'entreprise
    @Async  // envoi en arrière-plan
    public void sendConfirmationToEntreprise(Partenariat p) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(p.getEmailEntreprise());
            helper.setSubject("✅ Confirmation de votre demande de partenariat — PédiaNéphro");
            helper.setText(buildConfirmationHtml(p), true);

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Erreur envoi mail entreprise: " + e.getMessage());
        }
    }

    // Mail quand statut change (accepté ou refusé)
    public void sendStatutUpdateToEntreprise(Partenariat p) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(p.getEmailEntreprise());

            boolean CONFIRME = p.getStatut().name().equals("ACCEPTE");
            helper.setSubject(CONFIRME
                    ? "🎉 Votre partenariat a été accepté — PédiaNéphro"
                    : "❌ Votre demande de partenariat — PédiaNéphro");
            helper.setText(buildStatutHtml(p), true);

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Erreur envoi mail statut: " + e.getMessage());
        }
    }

    // ─── Templates HTML ────────────────────────────────────

    private String buildAdminHtml(Partenariat p) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
              <div style="background: linear-gradient(135deg, #4A90E2, #00B4D8); padding: 30px; border-radius: 12px 12px 0 0;">
                <h1 style="color: white; margin: 0;">🤝 Nouvelle demande de partenariat</h1>
              </div>
              <div style="background: #f8faff; padding: 30px; border: 1px solid #e2e8f0;">
                <table style="width: 100%; border-collapse: collapse;">
                  <tr>
                    <td style="padding: 10px; font-weight: bold; color: #64748b; width: 40%%;">🏢 Entreprise</td>
                    <td style="padding: 10px; color: #1e293b;">%s</td>
                  </tr>
                  <tr style="background: white;">
                    <td style="padding: 10px; font-weight: bold; color: #64748b;">📧 Email</td>
                    <td style="padding: 10px; color: #1e293b;">%s</td>
                  </tr>
                  <tr>
                    <td style="padding: 10px; font-weight: bold; color: #64748b;">📞 Téléphone</td>
                    <td style="padding: 10px; color: #1e293b;">%s</td>
                  </tr>
                  <tr style="background: white;">
                    <td style="padding: 10px; font-weight: bold; color: #64748b;">🌐 Site web</td>
                    <td style="padding: 10px; color: #1e293b;">%s</td>
                  </tr>
                  <tr>
                    <td style="padding: 10px; font-weight: bold; color: #64748b;">📅 Période</td>
                    <td style="padding: 10px; color: #1e293b;">%s → %s</td>
                  </tr>
                </table>
                <div style="margin-top: 20px; padding: 20px; background: white; border-radius: 10px; border-left: 4px solid #4A90E2;">
                  <p style="font-weight: bold; color: #64748b; margin: 0 0 10px;">💬 Message de collaboration :</p>
                  <p style="color: #334155; line-height: 1.6; margin: 0;">%s</p>
                </div>
                <div style="margin-top: 20px; text-align: center;">
                  <a href="http://localhost:4200/partenariats"
                     style="background: linear-gradient(135deg, #4A90E2, #00B4D8); color: white;
                            padding: 12px 30px; border-radius: 10px; text-decoration: none;
                            font-weight: bold;">
                    Gérer les partenariats →
                  </a>
                </div>
              </div>
            </div>
            """.formatted(
                p.getNomEntreprise(),
                p.getEmailEntreprise(),
                p.getTelephone(),
                p.getSiteWeb() != null ? p.getSiteWeb() : "—",
                p.getDateDebutCollaboration(),
                p.getDateFinCollaboration(),
                p.getMessageCollaboration()
        );
    }

    private String buildConfirmationHtml(Partenariat p) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
              <div style="background: linear-gradient(135deg, #4A90E2, #00B4D8); padding: 30px; border-radius: 12px 12px 0 0;">
                <h1 style="color: white; margin: 0;">✅ Demande reçue !</h1>
              </div>
              <div style="background: #f8faff; padding: 30px; border: 1px solid #e2e8f0;">
                <p style="color: #334155; font-size: 16px;">Bonjour <strong>%s</strong>,</p>
                <p style="color: #475569; line-height: 1.7;">
                  Nous avons bien reçu votre demande de partenariat avec <strong>PédiaNéphro</strong>.
                  Notre équipe l'examinera dans les plus brefs délais et vous contactera pour la suite.
                </p>
                <div style="background: white; border-radius: 10px; padding: 20px; border-left: 4px solid #06D6A0; margin: 20px 0;">
                  <p style="margin: 0; color: #059669; font-weight: bold;">
                    ⏳ Votre demande est en cours d'examen
                  </p>
                </div>
                <p style="color: #64748b; font-size: 0.9rem;">
                  Votre message : <em>"%s"</em>
                </p>
                <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 20px 0;">
                <p style="color: #94a3b8; font-size: 0.85rem; text-align: center;">
                  PédiaNéphro — Centre de Néphrologie Pédiatrique
                </p>
              </div>
            </div>
            """.formatted(
                p.getNomEntreprise(),
                p.getMessageCollaboration().substring(0,
                        Math.min(100, p.getMessageCollaboration().length())) + "..."
        );
    }

    private String buildStatutHtml(Partenariat p) {
        boolean CONFIRME = p.getStatut().name().equals("CONFIRME");
        String color = CONFIRME ? "#06D6A0" : "#ef4444";
        String emoji = CONFIRME ? "🎉" : "❌";
        String titre = CONFIRME ? "Partenariat accepté !" : "Demande non retenue";
        String message = CONFIRME
                ? "Nous sommes ravis de vous accueillir parmi nos partenaires ! Nous vous contacterons prochainement pour finaliser les détails de notre collaboration."
                : "Après examen de votre dossier, nous ne sommes pas en mesure de donner suite à votre demande pour le moment. Nous vous remercions de l'intérêt que vous portez à notre association.";

        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
              <div style="background: %s; padding: 30px; border-radius: 12px 12px 0 0;">
                <h1 style="color: white; margin: 0;">%s %s</h1>
              </div>
              <div style="background: #f8faff; padding: 30px; border: 1px solid #e2e8f0;">
                <p style="color: #334155; font-size: 16px;">Bonjour <strong>%s</strong>,</p>
                <p style="color: #475569; line-height: 1.7;">%s</p>
                <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 20px 0;">
                <p style="color: #94a3b8; font-size: 0.85rem; text-align: center;">
                  PédiaNéphro — Centre de Néphrologie Pédiatrique
                </p>
              </div>
            </div>
            """.formatted(color, emoji, titre, p.getNomEntreprise(), message);
    }


    @Async
    public void sendQRCodeInscription(Inscription inscription) {
        try {
            // ✅ Contenu du QR code
            String qrContent = String.format(
                    "INSCRIPTION #%d\nÉvénement: %s\nParticipant: %s %s\nStatut: ACCEPTÉ",
                    inscription.getIdInscription(),
                    inscription.getEvent().getNomEvent(),
                    inscription.getParticipant().getPrenom(),
                    inscription.getParticipant().getNom()
            );

            // ✅ Génère le QR code
            byte[] qrBytes = qrCodeService.generateQRCode(qrContent, 300, 300);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(inscription.getParticipant().getEmail());
            helper.setSubject("🎟️ Votre inscription est confirmée — "
                    + inscription.getEvent().getNomEvent());
            helper.setText(buildQRHtml(inscription), true);

            // ✅ Attache le QR code comme image inline
            helper.addInline("qrcode",
                    new ByteArrayResource(qrBytes), "image/png");

            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("Erreur envoi QR code: " + e.getMessage());
        }
    }

    private String buildQRHtml(Inscription inscription) {
        return """
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
          <div style="background: linear-gradient(135deg, #4A90E2, #00B4D8);
                      padding: 30px; border-radius: 12px 12px 0 0; text-align: center;">
            <h1 style="color: white; margin: 0;">🎟️ Inscription Confirmée !</h1>
          </div>

          <div style="background: #f8faff; padding: 30px; border: 1px solid #e2e8f0;">

            <p style="color: #334155; font-size: 16px;">
              Bonjour <strong>%s %s</strong>,
            </p>

            <p style="color: #475569; line-height: 1.7;">
              Votre inscription à l'événement <strong>%s</strong>
              a été <strong style="color: #059669;">acceptée</strong> !
              Présentez ce QR code à l'entrée de l'événement.
            </p>

            <!-- Infos événement -->
            <div style="background: white; border-radius: 12px; padding: 20px;
                        border-left: 4px solid #4A90E2; margin: 20px 0;">
              <p style="margin: 0 0 8px; color: #64748b;">
                📅 <strong>Date :</strong> %s
              </p>
              <p style="margin: 0 0 8px; color: #64748b;">
                📍 <strong>Lieu :</strong> %s
              </p>
              <p style="margin: 0; color: #64748b;">
                🎫 <strong>N° Inscription :</strong> #%d
              </p>
            </div>

            <!-- QR Code -->
            <div style="text-align: center; margin: 30px 0;">
              <p style="color: #334155; font-weight: bold; margin-bottom: 15px;">
                📱 Votre QR Code d'accès
              </p>
              <img src="cid:qrcode" alt="QR Code"
                style="width: 200px; height: 200px; border: 3px solid #4A90E2;
                       border-radius: 12px; padding: 10px; background: white;">
              <p style="color: #94a3b8; font-size: 0.82rem; margin-top: 10px;">
                Présentez ce QR code à l'entrée de l'événement
              </p>
            </div>

            <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 20px 0;">
            <p style="color: #94a3b8; font-size: 0.85rem; text-align: center;">
              PédiaNéphro — Centre de Néphrologie Pédiatrique
            </p>
          </div>
        </div>
        """.formatted(
                inscription.getParticipant().getPrenom(),
                inscription.getParticipant().getNom(),
                inscription.getEvent().getNomEvent(),
                inscription.getEvent().getDateDebut().toString(),
                inscription.getEvent().getLieu(),
                inscription.getIdInscription()
        );
    }
// ✅ Ajoute ces deux méthodes dans ton MailService existant

    @Async
    public void sendListeAttenteNotification(Inscription inscription) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(inscription.getParticipant().getEmail());
            helper.setSubject("⏳ Vous êtes sur liste d'attente — "
                    + inscription.getEvent().getNomEvent());
            helper.setText(buildListeAttenteHtml(inscription), true);

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Erreur mail liste attente: " + e.getMessage());
        }
    }

    @Async
    public void sendPromotionListeAttenteNotification(Inscription inscription) {
        try {
            String qrContent = String.format(
                    "INSCRIPTION #%d\nÉvénement: %s\nParticipant: %s %s\nStatut: CONFIRMÉ",
                    inscription.getIdInscription(),
                    inscription.getEvent().getNomEvent(),
                    inscription.getParticipant().getPrenom(),
                    inscription.getParticipant().getNom()
            );

            byte[] qrBytes = qrCodeService.generateQRCode(qrContent, 300, 300);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(inscription.getParticipant().getEmail());
            helper.setSubject("🎉 Une place s'est libérée ! — "
                    + inscription.getEvent().getNomEvent());
            helper.setText(buildPromotionHtml(inscription), true);
            helper.addInline("qrcode",
                    new ByteArrayResource(qrBytes), "image/png");

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Erreur mail promotion: " + e.getMessage());
        }
    }

    private String buildListeAttenteHtml(Inscription inscription) {
        return """
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
          <div style="background: linear-gradient(135deg, #f59e0b, #d97706);
                      padding: 30px; border-radius: 12px 12px 0 0; text-align: center;">
            <h1 style="color: white; margin: 0;">⏳ Liste d'attente</h1>
          </div>
          <div style="background: #f8faff; padding: 30px; border: 1px solid #e2e8f0;">
            <p style="color: #334155; font-size: 16px;">
              Bonjour <strong>%s %s</strong>,
            </p>
            <p style="color: #475569; line-height: 1.7;">
              L'événement <strong>%s</strong> est actuellement complet.
              Vous avez été automatiquement placé(e) sur la
              <strong>liste d'attente</strong>.
            </p>
            <div style="background: white; border-radius: 12px; padding: 20px;
                        border-left: 4px solid #f59e0b; margin: 20px 0;">
              <p style="margin: 0; color: #d97706; font-weight: bold;">
                ⏳ Nous vous contacterons automatiquement si une place se libère.
              </p>
            </div>
            <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 20px 0;">
            <p style="color: #94a3b8; font-size: 0.85rem; text-align: center;">
              PédiaNéphro — Centre de Néphrologie Pédiatrique
            </p>
          </div>
        </div>
        """.formatted(
                inscription.getParticipant().getPrenom(),
                inscription.getParticipant().getNom(),
                inscription.getEvent().getNomEvent()
        );
    }

    private String buildPromotionHtml(Inscription inscription) {
        return """
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
          <div style="background: linear-gradient(135deg, #06D6A0, #059669);
                      padding: 30px; border-radius: 12px 12px 0 0; text-align: center;">
            <h1 style="color: white; margin: 0;">🎉 Une place s'est libérée !</h1>
          </div>
          <div style="background: #f8faff; padding: 30px; border: 1px solid #e2e8f0;">
            <p style="color: #334155; font-size: 16px;">
              Bonjour <strong>%s %s</strong>,
            </p>
            <p style="color: #475569; line-height: 1.7;">
              Bonne nouvelle ! Une place s'est libérée pour l'événement
              <strong>%s</strong>. Votre inscription a été
              <strong style="color: #059669;">automatiquement confirmée</strong> !
            </p>
            <div style="text-align: center; margin: 30px 0;">
              <p style="color: #334155; font-weight: bold; margin-bottom: 15px;">
                📱 Votre QR Code d'accès
              </p>
              <img src="cid:qrcode" alt="QR Code"
                style="width: 200px; height: 200px; border: 3px solid #06D6A0;
                       border-radius: 12px; padding: 10px; background: white;">
              <p style="color: #94a3b8; font-size: 0.82rem; margin-top: 10px;">
                Présentez ce QR code à l'entrée de l'événement
              </p>
            </div>
            <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 20px 0;">
            <p style="color: #94a3b8; font-size: 0.85rem; text-align: center;">
              PédiaNéphro — Centre de Néphrologie Pédiatrique
            </p>
          </div>
        </div>
        """.formatted(
                inscription.getParticipant().getPrenom(),
                inscription.getParticipant().getNom(),
                inscription.getEvent().getNomEvent()
        );
    }

    @Async
    public void sendCertificatParticipation(Inscription inscription) {
        try {
            byte[] pdfBytes = certificatService.generateCertificat(inscription);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(inscription.getParticipant().getEmail());
            helper.setSubject("🎓 Votre certificat de participation — "
                    + inscription.getEvent().getNomEvent());
            helper.setText(buildCertificatHtml(inscription), true);

            // ✅ PDF en pièce jointe
            helper.addAttachment(
                    "Certificat_" + inscription.getParticipant().getNom() + ".pdf",
                    new ByteArrayResource(pdfBytes),
                    "application/pdf"
            );

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Erreur envoi certificat: " + e.getMessage());
        }
    }

    private String buildCertificatHtml(Inscription inscription) {
        return """
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
          <div style="background: linear-gradient(135deg, #1a56a4, #4A90E2);
                      padding: 30px; border-radius: 12px 12px 0 0; text-align: center;
                      border-top: 4px solid #b89432;">
            <h1 style="color: white; margin: 0; letter-spacing: 2px;">
              🎓 CERTIFICAT DE PARTICIPATION
            </h1>
            <p style="color: rgba(255,255,255,0.8); margin: 5px 0 0;">
              PédiaNéphro — Centre de Néphrologie Pédiatrique
            </p>
          </div>
          <div style="background: #f8faff; padding: 30px; border: 1px solid #e2e8f0;">
            <p style="color: #334155; font-size: 16px;">
              Bonjour <strong>%s %s</strong>,
            </p>
            <p style="color: #475569; line-height: 1.8; font-size: 15px;">
              Nous vous remercions chaleureusement pour votre participation à
              l'événement <strong style="color: #1a56a4;">%s</strong>.
            </p>
            <div style="background: white; border-radius: 12px; padding: 20px;
                        border-left: 4px solid #b89432; margin: 20px 0;
                        box-shadow: 0 2px 8px rgba(0,0,0,0.06);">
              <p style="margin: 0 0 8px; color: #64748b;">
                🎟️ <strong>Événement :</strong> %s
              </p>
              <p style="margin: 0 0 8px; color: #64748b;">
                📅 <strong>Date :</strong> %s
              </p>
              <p style="margin: 0; color: #64748b;">
                📍 <strong>Lieu :</strong> %s
              </p>
            </div>
            <div style="background: linear-gradient(135deg, #f0f7ff, #e8f4ff);
                        border-radius: 12px; padding: 20px; text-align: center;
                        margin: 20px 0; border: 1px solid #bfdbfe;">
              <p style="margin: 0; color: #1a56a4; font-weight: bold; font-size: 15px;">
                📎 Votre certificat PDF officiel est en pièce jointe
              </p>
              <p style="margin: 8px 0 0; color: #64748b; font-size: 13px;">
                Conservez-le précieusement pour votre dossier professionnel
              </p>
            </div>
            <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 20px 0;">
            <p style="color: #94a3b8; font-size: 0.82rem; text-align: center;">
              PédiaNéphro — Centre de Néphrologie Pédiatrique
            </p>
          </div>
        </div>
        """.formatted(
                inscription.getParticipant().getPrenom(),
                inscription.getParticipant().getNom(),
                inscription.getEvent().getNomEvent(),
                inscription.getEvent().getNomEvent(),
                inscription.getEvent().getDateDebut()
                        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                inscription.getEvent().getLieu()
        );
    }
}