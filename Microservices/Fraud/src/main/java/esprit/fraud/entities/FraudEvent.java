package esprit.fraud.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    
    private String action; // ex: LOGIN, RECLAMATION_CREATE, etc.
    
    private String ipAddress;
    
    private String deviceInfo;
    
    private Double score;
    
    private Boolean suspicious;
    
    private String details;
    
    private LocalDateTime createdAt;
}
