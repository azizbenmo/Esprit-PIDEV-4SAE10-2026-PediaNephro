package esprit.fraud.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FraudAnalyzeRequest {
    
    private Long userId;

    @NotBlank(message = "L'action est obligatoire")
    private String action;

    private String ipAddress;
    
    private String deviceInfo;

    private String email; // Email de l'utilisateur pour les alertes de fraude
}
