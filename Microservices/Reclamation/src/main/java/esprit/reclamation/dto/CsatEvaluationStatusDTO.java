package esprit.reclamation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CsatEvaluationStatusDTO {

    private boolean existe;
    private CsatEvaluationResponseDTO evaluation;
}
