package tn.example.events.Entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEvent;

    private String nomEvent;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String description;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private String lieu;
    //private String imageUrl;   // URL de l'image principale de l'événement

    private Integer capacite;  // nombre de places total

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String imageBase64;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String videoBase64;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean archive = false;

    // Relation avec Inscription
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Inscription> inscriptions;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "partenariat_id")
    @JsonIgnoreProperties({"events", "hibernateLazyInitializer"})
    private Partenariat partenariat;


    @Transient
    public int getPlacesRestantes() {
        if (capacite == null) return 0;
        long inscritsAcceptes = inscriptions == null ? 0 :
                inscriptions.stream()
                        .filter(i -> i.getStatut().name().equals("CONFIRME")) //uniquement ACCEPTE
                        .count();
        return (int) (capacite - inscritsAcceptes);
    }

}
