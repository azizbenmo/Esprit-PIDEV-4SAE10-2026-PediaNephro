package esprit.reclamation.dto;

import esprit.reclamation.entities.StatutReclamation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReponseAdminDTO {

    @NotNull(message = "L'identifiant admin (adminId) est obligatoire.")
    private Long adminId;

    @NotBlank(message = "La reponse de l'admin est obligatoire.")
    private String reponse;

    @NotNull(message = "Le statut est obligatoire.")
    private StatutReclamation statut;
}
