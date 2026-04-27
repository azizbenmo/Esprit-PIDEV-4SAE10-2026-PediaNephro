package esprit.User.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Convert(converter = RoleAttributeConverter.class)
    @Column(nullable = false, length = 32)
    private Role role;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String faceDescriptor;

    @Column(nullable = false)
    private Boolean faceIdEnabled = false;

    @Column(nullable = false)
    private Integer failedLoginAttempts = 0;

    private LocalDateTime loginLockedUntil;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnore
    private Parent parent;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() { this.createdAt = LocalDateTime.now(); }

    @PreUpdate
    public void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    // Getters & Setters
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public String getFaceDescriptor() { return faceDescriptor; }
    public void setFaceDescriptor(String faceDescriptor) { this.faceDescriptor = faceDescriptor; }
    public Boolean getFaceIdEnabled() { return faceIdEnabled; }
    public void setFaceIdEnabled(Boolean faceIdEnabled) { this.faceIdEnabled = faceIdEnabled; }
    public Integer getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(Integer failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }
    public LocalDateTime getLoginLockedUntil() { return loginLockedUntil; }
    public void setLoginLockedUntil(LocalDateTime loginLockedUntil) { this.loginLockedUntil = loginLockedUntil; }
    public Parent getParent() { return parent; }
    public void setParent(Parent parent) { this.parent = parent; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
