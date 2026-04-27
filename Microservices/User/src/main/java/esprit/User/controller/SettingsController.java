package esprit.User.controller;

import esprit.User.dto.ApiResponse;
import esprit.User.dto.ChangePasswordRequest;
import esprit.User.security.audit.LogAction;
import esprit.User.dto.FaceIdSettingsRequest;
import esprit.User.services.SettingsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/mic1/settings")
public class SettingsController {


    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @PutMapping("/change-password")
    @LogAction(action = "CHANGE_PASSWORD")
    public ResponseEntity<ApiResponse<Map<String, Object>>> changePassword(@RequestBody ChangePasswordRequest request) {
        Map<String, Object> data = settingsService.changePassword(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Mot de passe modifie avec succes", data));
    }

    @PutMapping("/face-id")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateFaceId(@RequestBody FaceIdSettingsRequest request) {
        try {
            Map<String, Object> data = settingsService.updateFaceId(request);
            String message = Boolean.TRUE.equals(request.getEnabled())
                    ? "Face ID active avec succes"
                    : "Face ID desactive avec succes";
            return ResponseEntity.ok(new ApiResponse<>(true, message, data));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, ex.getMessage(), null));
        }
    }
}
