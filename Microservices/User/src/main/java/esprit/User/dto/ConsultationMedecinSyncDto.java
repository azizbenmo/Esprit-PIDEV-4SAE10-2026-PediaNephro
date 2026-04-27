package esprit.User.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Corps JSON pour {@code POST /apiConsultation/internal/medecin/sync}. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationMedecinSyncDto {

    private Long userId;
    private String email;
    private String fullName;
    private String specialite;
    private String telephone;
    private Integer anneesExperience;
    private Boolean disponible;
    private String ville;
}
