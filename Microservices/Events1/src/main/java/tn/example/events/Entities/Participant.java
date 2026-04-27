package tn.example.events.Entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idParticipant;

    private String nom;
    private String prenom;
    private String email;
    private String telephone;


    // Relation avec Inscription
    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Inscription> inscriptions;


}
