package esprit.User.controller;

import esprit.User.dto.ProfileResponseDto;
import esprit.User.services.ProfileService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("userProfileController")
@RequestMapping(value = "/mic1/profile", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProfileController {


    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<ProfileResponseDto> getMyProfile() {
        return ResponseEntity.ok(profileService.getConnectedUserProfile());
    }
}
