package esprit.User.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "admins")
public class Admin {

    @Id
    private Long id;

    private String fullName;
    private String phone;
    private String position;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    // Getters & Setters
    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
