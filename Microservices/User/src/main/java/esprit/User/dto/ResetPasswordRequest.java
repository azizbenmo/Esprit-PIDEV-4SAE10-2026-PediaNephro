package esprit.User.dto;

/**
 * DTO pour la réinitialisation effective du mot de passe.
 * Contient le token reçu par email et le nouveau mot de passe.
 */
public class ResetPasswordRequest {

    private String token;
    private String newPassword;

    public ResetPasswordRequest() {
    }

    public ResetPasswordRequest(String token, String newPassword) {
        this.token = token;
        this.newPassword = newPassword;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
