package esprit.User.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogRequest {
    private String action;      // Connexion, Déconnexion...
    private String timestamp;   // Optionnel, le backend peut aussi utiliser sa propre heure
    private String device;      // Windows, iPhone...
    private String browser;     // Chrome, Firefox...
    private String details;     // Optionnel
}
