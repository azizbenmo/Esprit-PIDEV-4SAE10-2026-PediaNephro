package esprit.reclamation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CsatEvaluationResponseDTO {

    private Long id;
    private Long reclamationId;
    private Long userId;
    private Integer note;
    private String commentaire;
    private LocalDateTime dateEvaluation;
}
