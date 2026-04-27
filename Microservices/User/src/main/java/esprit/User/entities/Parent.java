package esprit.User.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "parents")
public class Parent {

    @Id
    private Long id;

    private String fullName;
    private String phone;
    private String address;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @JsonIgnore // important : évite la boucle infinie Parent -> children -> parent -> ...
    private List<Patient> children;

    // Getters & Setters
    public Long getId() { return id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<Patient> getChildren() { return children; }
    public void setChildren(List<Patient> children) { this.children = children; }
}