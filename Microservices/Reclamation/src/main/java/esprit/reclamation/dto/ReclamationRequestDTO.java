package esprit.reclamation.dto;

import esprit.reclamation.entities.Priorite;
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
public class ReclamationRequestDTO {

    @NotBlank(message = "Le titre est obligatoire.")
    private String titre;

    @NotBlank(message = "La description est obligatoire.")
    private String description;

    /**
     * Optionnel : à la création, la priorité est recalculée par {@link esprit.reclamation.services.CategorisationService}.
     */
    private Priorite priorite;

    @NotNull(message = "L'identifiant utilisateur (userId) est obligatoire.")
    private Long userId;
}
