package esprit.User.dto;

/**
 * DTO pour la demande de réinitialisation de mot de passe.
 * Contient uniquement l'email de l'utilisateur.
 */
public class ForgotPasswordRequest {

    private String email;

    public ForgotPasswordRequest() {
    }

    public ForgotPasswordRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
