package esprit.User.dto;

public class PatientSyncResponseDto {
    private Long id;         // id généré côté User (sera stocké dans enfant.userPatientId)
    private String fullName;

    public PatientSyncResponseDto(Long id, String fullName) {
        this.id = id;
        this.fullName = fullName;
    }
    public Long getId() { return id; }
    public String getFullName() { return fullName; }
}