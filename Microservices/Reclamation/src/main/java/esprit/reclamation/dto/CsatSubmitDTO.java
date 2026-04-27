package esprit.reclamation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CsatSubmitDTO {

    @NotNull(message = "L'identifiant utilisateur est obligatoire.")
    private Long userId;

    @NotNull(message = "La note est obligatoire.")
    @Min(value = 1, message = "La note doit être au moins 1.")
    @Max(value = 5, message = "La note doit être au plus 5.")
    private Integer note;

    private String commentaire;
}
