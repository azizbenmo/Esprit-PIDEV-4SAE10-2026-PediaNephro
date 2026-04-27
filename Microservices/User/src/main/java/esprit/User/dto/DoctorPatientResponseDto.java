package esprit.User.dto;

import java.time.LocalDate;

public class DoctorPatientResponseDto {
    private Long id;
    private String fullName;
    private LocalDate birthDate;
    private String gender;
    private Long parentId;
    private String parentName;

    public DoctorPatientResponseDto(Long id, String fullName, LocalDate birthDate, String gender, Long parentId, String parentName) {
        this.id = id;
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.parentId = parentId;
        this.parentName = parentName;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getGender() {
        return gender;
    }

    public Long getParentId() {
        return parentId;
    }

    public String getParentName() {
        return parentName;
    }
}
