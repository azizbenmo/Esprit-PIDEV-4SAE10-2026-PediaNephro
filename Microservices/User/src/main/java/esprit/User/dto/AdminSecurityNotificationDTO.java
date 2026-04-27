package esprit.User.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminSecurityNotificationDTO {
    private Long id;
    private String type;
    private String message;
    private String clientIp;
    private String userAgentSnippet;
    private String usernameHint;
    private boolean seenByAdmin;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime createdAt;
}
