package tn.example.events.Entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Partenariat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPartenariat;

    private String nomEntreprise;
    private String emailEntreprise;
    private String telephone;
    private String siteWeb;
    //private String secteurActivite;


    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String logo;

    private LocalDate dateDebutCollaboration;
    private LocalDate dateFinCollaboration;

    @Enumerated(EnumType.STRING)
    private StatutPartenariat statut = StatutPartenariat.EN_ATTENTE;

    private LocalDate dateDemande = LocalDate.now();

    private String MessageCollaboration;

    /*@Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean archive = false;*/
    @OneToMany(mappedBy = "partenariat", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Event> events;
}
