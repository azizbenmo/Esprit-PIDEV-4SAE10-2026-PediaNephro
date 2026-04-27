package esprit.User.dto;

/**
 * Option d’inscription événement : compte User (id = users.id = doctor.id ou patient.id avec MapsId).
 */
public class EventInscriptionOptionDto {

    private Long userId;
    private String fullName;
    private String email;

    public EventInscriptionOptionDto() {
    }

    public EventInscriptionOptionDto(Long userId, String fullName, String email) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
