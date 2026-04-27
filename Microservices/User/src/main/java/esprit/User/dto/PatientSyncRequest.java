package esprit.User.dto;

import java.time.LocalDate;

public class PatientSyncRequest {
    private String fullName;
    private LocalDate birthDate;
    private String gender;
    private Long parentId;

    // Constructeur vide obligatoire pour Jackson
    public PatientSyncRequest() {}

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
}