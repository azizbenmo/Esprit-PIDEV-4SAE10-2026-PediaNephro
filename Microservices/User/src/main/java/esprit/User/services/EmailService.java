package esprit.User.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;


@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Envoie un email simple (texte brut).
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    /**
     * Envoie un email HTML pour la réinitialisation de mot de passe.
     */
    public void sendPasswordResetEmail(String to, String token) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;

        String subject = "PediaNephro - Réinitialisation de votre mot de passe";

        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4A90D9; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background-color: #f9f9f9; padding: 30px; border-radius: 0 0 8px 8px; }
                    .button { display: inline-block; background-color: #4A90D9; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .button:hover { background-color: #357ABD; }
                    .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #666; }
                    .warning { background-color: #fff3cd; border: 1px solid #ffc107; padding: 10px; border-radius: 5px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🏥 PediaNephro</h1>
                    </div>
                    <div class="content">
                        <h2>Réinitialisation de mot de passe</h2>
                        <p>Bonjour,</p>
                        <p>Vous avez demandé la réinitialisation de votre mot de passe. Cliquez sur le bouton ci-dessous pour créer un nouveau mot de passe :</p>
                        
                        <div style="text-align: center;">
                            <a href="%s" class="button" style="color: white;">Réinitialiser mon mot de passe</a>
                        </div>
                        
                        <p>Ou copiez ce lien dans votre navigateur :</p>
                        <p style="word-break: break-all; background-color: #eee; padding: 10px; border-radius: 5px;">%s</p>
                        
                        <div class="warning">
                            <strong>⚠️ Important :</strong> Ce lien expire dans <strong>15 minutes</strong>. Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.
                        </div>
                    </div>
                    <div class="footer">
                        <p>© 2024 PediaNephro - Tous droits réservés</p>
                        <p>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(resetLink, resetLink);

        sendHtmlEmail(to, subject, htmlContent);
    }

    public void sendVerificationEmail(String to, String token) {
        String verifyLink = frontendUrl + "/verify-email?token=" + token;
        String subject = "PediaNephro - Verification de votre email";
        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4A90D9; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background-color: #f9f9f9; padding: 30px; border-radius: 0 0 8px 8px; }
                    .button { display: inline-block; background-color: #4A90D9; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>PediaNephro</h1>
                    </div>
                    <div class="content">
                        <h2>Verification de votre adresse email</h2>
                        <p>Bienvenue,</p>
                        <p>Cliquez sur le bouton ci-dessous pour activer votre compte :</p>
                        <div style="text-align: center;">
                            <a href="%s" class="button" style="color: white;">Verifier mon email</a>
                        </div>
                        <p>Ou copiez ce lien dans votre navigateur :</p>
                        <p style="word-break: break-all; background-color: #eee; padding: 10px; border-radius: 5px;">%s</p>
                        <p>Ce lien expire dans 24 heures.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(verifyLink, verifyLink);

        sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * Envoie un email au format HTML.
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email: " + e.getMessage(), e);
        }
    }
}
