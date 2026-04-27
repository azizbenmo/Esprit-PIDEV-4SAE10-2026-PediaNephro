package esprit.reclamation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FraudAnalyzeRequest {
    private Long userId;
    private String action;
    private String ipAddress;
    private String deviceInfo;
}
