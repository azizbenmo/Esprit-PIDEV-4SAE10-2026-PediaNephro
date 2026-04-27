package esprit.reclamation.services;

import esprit.reclamation.entities.CategorieReclamation;
import esprit.reclamation.entities.Priorite;
import esprit.reclamation.entities.PrioriteReclamation;
import esprit.reclamation.entities.Reclamation;
import esprit.reclamation.entities.StatutReclamation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CategorisationServiceTest {

    private CategorisationService service;

    @BeforeEach
    void setUp() {
        service = new CategorisationService();
    }

    @Test
    void enrichir_detecteFacturationEtSla24h() {
        Reclamation r = base("Litige", "Problème de facturation et remboursement");
        service.enrichir(r);
        assertThat(r.getCategorie()).isEqualTo(CategorieReclamation.FACTURATION);
        assertThat(r.getPrioriteReclamation()).isNotNull();
        assertThat(r.getSlaDeadline()).isNotNull();
    }

    @Test
    void enrichir_detecteTechnique() {
        Reclamation r = base("Bug", "La page est lente et plante au chargement");
        service.enrichir(r);
        assertThat(r.getCategorie()).isEqualTo(CategorieReclamation.TECHNIQUE);
    }

    @Test
    void enrichir_prioriteCritique_siUrgent() {
        Reclamation r = base("Urgent", "Situation critique côté accès");
        service.enrichir(r);
        assertThat(r.getPrioriteReclamation()).isEqualTo(PrioriteReclamation.CRITIQUE);
        assertThat(r.getPriorite()).isEqualTo(Priorite.URGENTE);
    }

    @Test
    void enrichir_categorieAutre_siAucunMotCle() {
        Reclamation r = base("Question", "Message général sans mot spécifique");
        service.enrichir(r);
        assertThat(r.getCategorie()).isEqualTo(CategorieReclamation.AUTRE);
    }

    private static Reclamation base(String titre, String description) {
        return Reclamation.builder()
                .titre(titre)
                .description(description)
                .statut(StatutReclamation.EN_ATTENTE)
                .priorite(Priorite.MOYENNE)
                .userId(1L)
                .escaladee(false)
                .build();
    }
}
