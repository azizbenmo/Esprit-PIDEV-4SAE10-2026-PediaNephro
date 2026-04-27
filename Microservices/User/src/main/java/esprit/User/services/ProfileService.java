package esprit.User.services;

import esprit.User.dto.ProfileResponseDto;
import esprit.User.entities.Doctor;
import esprit.User.entities.Parent;
import esprit.User.entities.Role;
import esprit.User.entities.User;
import esprit.User.repositories.AdminRepository;
import esprit.User.repositories.DoctorRepository;
import esprit.User.repositories.ParentRepository;
import esprit.User.repositories.PatientRepository;
import esprit.User.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final ParentRepository parentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public ProfileService(UserRepository userRepository,
                          AdminRepository adminRepository,
                          ParentRepository parentRepository,
                          DoctorRepository doctorRepository,
                          PatientRepository patientRepository) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.parentRepository = parentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    public ProfileResponseDto getConnectedUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()
                || "anonymousUser".equalsIgnoreCase(authentication.getName())) {
            throw new RuntimeException("Utilisateur non authentifie");
        }

        String usernameOrEmail = authentication.getName();
        User user = userRepository.findFirstByUsernameOrEmail(usernameOrEmail).orElse(null);
        if (user == null ) {
            throw new RuntimeException("Utilisateur connecte introuvable");
        }

        ProfileResponseDto dto = new ProfileResponseDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());

        if (user.getRole() == Role.PARENT) {
            parentRepository.findByUser_Id(user.getId()).ifPresent(parent -> {
                dto.setFullName(parent.getFullName());
                dto.setPhone(parent.getPhone());
                dto.setAddress(parent.getAddress());
                dto.setTotalChildren(patientRepository.countByParent_Id(parent.getId()));
            });
        } else if (user.getRole() == Role.DOCTOR) {
            doctorRepository.findByUser_Id(user.getId()).ifPresent(doctor -> {
                dto.setFullName(doctor.getFullName());
                dto.setPhone(doctor.getPhone());
                dto.setSpeciality(doctor.getSpeciality());
                dto.setLicenseNumber(doctor.getLicenseNumber());
            });
        } else if (user.getRole() == Role.ADMIN) {
            adminRepository.findById(user.getId()).ifPresent(admin -> {
                dto.setFullName(admin.getFullName());
                dto.setPhone(admin.getPhone());
            });
        }

        if (dto.getFullName() == null || dto.getFullName().isBlank()) {
            dto.setFullName(user.getUsername());
        }

        return dto;
    }
}
