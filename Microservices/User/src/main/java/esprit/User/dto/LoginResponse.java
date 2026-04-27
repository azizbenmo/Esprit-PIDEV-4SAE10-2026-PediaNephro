package esprit.User.dto;

public class LoginResponse {
    private String message;
    private String token;
    private String role;
    private String username;
    private String email;
    private Long parentId;
    private Long doctorId;
    private Long id;

    public LoginResponse(String message, String token, String role, String username, String email, Long parentId, Long doctorId, Long id) {
        this.message = message;
        this.token = token;
        this.role = role;
        this.username = username;
        this.email = email;
        this.parentId = parentId;
        this.doctorId = doctorId;
        this.id = id;
    }

    public LoginResponse(String token, String role, String username, String email, Long parentId, Long doctorId, Long id) {
        this.token = token;
        this.role = role;
        this.username = username;
        this.email = email;
        this.parentId = parentId;
        this.doctorId = doctorId;
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public String getRole() {
        return role;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Long getParentId() {
        return parentId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public Long getId() {
        return id;
    }
}
