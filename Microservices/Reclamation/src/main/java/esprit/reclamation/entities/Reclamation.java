package esprit.reclamation.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reclamations")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reclamation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String titre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutReclamation statut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priorite priorite;

    @Column(nullable = false)
    private Long userId;

    private Long adminId;

    @Column(columnDefinition = "TEXT")
    private String reponse;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    private LocalDateTime dateTraitement;

    @Enumerated(EnumType.STRING)
    private CategorieReclamation categorie;

    @Enumerated(EnumType.STRING)
    @Column(name = "priorite_reclamation")
    private PrioriteReclamation prioriteReclamation;

    private LocalDateTime slaDeadline;

    @Column(nullable = false)
    private Boolean escaladee;

    @PrePersist
    public void prePersist() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
        if (statut == null) {
            statut = StatutReclamation.EN_ATTENTE;
        }
        if (priorite == null) {
            priorite = Priorite.MOYENNE;
        }
        if (prioriteReclamation == null) {
            prioriteReclamation = PrioriteReclamation.NORMALE;
        }
        if (escaladee == null) {
            escaladee = false;
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (dateTraitement == null
                && (statut == StatutReclamation.RESOLUE
                        || statut == StatutReclamation.REJETEE
                        || statut == StatutReclamation.CLOTUREE)) {
            dateTraitement = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }

    public String getDescription() {
        return description;
    }

    public StatutReclamation getStatut() {
        return statut;
    }

    public Priorite getPriorite() {
        return priorite;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getAdminId() {
        return adminId;
    }

    public String getReponse() {
        return reponse;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public LocalDateTime getDateTraitement() {
        return dateTraitement;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatut(StatutReclamation statut) {
        this.statut = statut;
    }

    public void setPriorite(Priorite priorite) {
        this.priorite = priorite;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public void setReponse(String reponse) {
        this.reponse = reponse;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public void setDateTraitement(LocalDateTime dateTraitement) {
        this.dateTraitement = dateTraitement;
    }

    public CategorieReclamation getCategorie() {
        return categorie;
    }

    public void setCategorie(CategorieReclamation categorie) {
        this.categorie = categorie;
    }

    public PrioriteReclamation getPrioriteReclamation() {
        return prioriteReclamation;
    }

    public void setPrioriteReclamation(PrioriteReclamation prioriteReclamation) {
        this.prioriteReclamation = prioriteReclamation;
    }

    public LocalDateTime getSlaDeadline() {
        return slaDeadline;
    }

    public void setSlaDeadline(LocalDateTime slaDeadline) {
        this.slaDeadline = slaDeadline;
    }

    public Boolean getEscaladee() {
        return escaladee;
    }

    public void setEscaladee(Boolean escaladee) {
        this.escaladee = escaladee;
    }
}
