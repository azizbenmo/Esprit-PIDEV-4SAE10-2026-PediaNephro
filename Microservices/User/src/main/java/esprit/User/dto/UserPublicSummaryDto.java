package esprit.User.dto;

/**
 * Résumé public d'un utilisateur pour appels inter-services (sans mot de passe).
 */
public class UserPublicSummaryDto {

    private Long id;
    private String username;
    private String email;
    private String role;
    private Boolean active;

    public UserPublicSummaryDto() {
    }

    public UserPublicSummaryDto(Long id, String username, String email, String role, Boolean active) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
